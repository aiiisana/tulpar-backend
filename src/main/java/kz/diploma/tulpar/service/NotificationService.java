package kz.diploma.tulpar.service;

import kz.diploma.tulpar.domain.entity.Notification;
import kz.diploma.tulpar.domain.entity.User;
import kz.diploma.tulpar.dto.response.NotificationResponse;
import kz.diploma.tulpar.dto.response.PageResponse;
import kz.diploma.tulpar.exception.ResourceNotFoundException;
import kz.diploma.tulpar.repository.NotificationRepository;
import kz.diploma.tulpar.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class NotificationService {

    private final NotificationRepository notificationRepository;
    private final UserRepository userRepository;
    private final DeviceTokenService deviceTokenService;
    private final FcmService fcmService;

    // ── Read ──────────────────────────────────────────────────────────────────

    @Transactional(readOnly = true)
    public PageResponse<NotificationResponse> findByUser(String userId, int page, int size) {
        return PageResponse.from(
                notificationRepository.findAllByUserIdOrderByCreatedAtDesc(
                        userId, PageRequest.of(page, size))
                        .map(this::toResponse));
    }

    @Transactional
    public NotificationResponse markRead(String userId, UUID notificationId) {
        Notification notification = notificationRepository
                .findByIdAndUserId(notificationId, userId)
                .orElseThrow(() -> ResourceNotFoundException.of("Notification", notificationId));
        notification.setRead(true);
        return toResponse(notificationRepository.save(notification));
    }

    // ── Create + push ─────────────────────────────────────────────────────────

    /**
     * Create an in-app notification for a specific user and immediately
     * fire a push notification to all their registered devices.
     */
    @Transactional
    public NotificationResponse createAndPush(String userId, String title, String body) {
        User user = userRepository.getReferenceById(userId);

        Notification saved = notificationRepository.save(Notification.builder()
                .user(user)
                .title(title)
                .body(body)
                .build());

        // Send push asynchronously so the DB transaction commits first
        pushToUser(userId, title, body);

        return toResponse(saved);
    }

    /**
     * Broadcast a push notification to ALL users who have notifications enabled.
     * No in-app notification row is created — use this for global events
     * (daily reminders, app-wide announcements).
     */
    public void broadcastPush(String title, String body) {
        List<String> tokens = deviceTokenService.getAllEnabledTokens();
        if (tokens.isEmpty()) {
            log.info("[Notification] Broadcast skipped — no registered tokens");
            return;
        }
        log.info("[Notification] Broadcasting push to {} devices", tokens.size());
        fcmService.sendToDevices(tokens, title, body);
    }

    // ── Internal helpers ──────────────────────────────────────────────────────

    @Async
    protected void pushToUser(String userId, String title, String body) {
        List<String> tokens = deviceTokenService.getTokensForUser(userId);
        for (String token : tokens) {
            boolean ok = fcmService.sendToDevice(token, title, body);
            if (!ok) {
                deviceTokenService.removeStaleToken(token);
            }
        }
    }

    private NotificationResponse toResponse(Notification n) {
        return NotificationResponse.builder()
                .id(n.getId())
                .title(n.getTitle())
                .body(n.getBody())
                .read(n.isRead())
                .createdAt(n.getCreatedAt())
                .build();
    }
}
