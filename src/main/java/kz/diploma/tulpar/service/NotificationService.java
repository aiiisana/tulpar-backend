package kz.diploma.tulpar.service;

import kz.diploma.tulpar.domain.entity.Notification;
import kz.diploma.tulpar.dto.response.NotificationResponse;
import kz.diploma.tulpar.dto.response.PageResponse;
import kz.diploma.tulpar.exception.ResourceNotFoundException;
import kz.diploma.tulpar.repository.NotificationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class NotificationService {

    private final NotificationRepository notificationRepository;

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
