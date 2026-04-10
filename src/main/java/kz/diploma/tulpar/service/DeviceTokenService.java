package kz.diploma.tulpar.service;

import kz.diploma.tulpar.domain.entity.DeviceToken;
import kz.diploma.tulpar.domain.entity.User;
import kz.diploma.tulpar.repository.DeviceTokenRepository;
import kz.diploma.tulpar.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class DeviceTokenService {

    private final DeviceTokenRepository deviceTokenRepository;
    private final UserRepository userRepository;

    /**
     * Register or refresh an FCM token for the given user.
     * If the token already exists it is updated (user may have changed device).
     * If it is new, a fresh row is inserted.
     */
    @Transactional
    public void registerToken(String userId, String token, String platform) {
        deviceTokenRepository.findByToken(token).ifPresentOrElse(
                existing -> {
                    // Token already in DB — update user association + platform
                    existing.setUser(userRepository.getReferenceById(userId));
                    existing.setPlatform(platform);
                    deviceTokenRepository.save(existing);
                    log.debug("[DeviceToken] Updated token for user={}", userId);
                },
                () -> {
                    User user = userRepository.getReferenceById(userId);
                    deviceTokenRepository.save(DeviceToken.builder()
                            .user(user)
                            .token(token)
                            .platform(platform)
                            .build());
                    log.info("[DeviceToken] Registered new token for user={} platform={}", userId, platform);
                }
        );
    }

    /** Returns all FCM tokens belonging to a specific user. */
    @Transactional(readOnly = true)
    public List<String> getTokensForUser(String userId) {
        return deviceTokenRepository.findAllByUserId(userId)
                .stream()
                .map(DeviceToken::getToken)
                .toList();
    }

    /** Returns tokens for all users who have notifications enabled. */
    @Transactional(readOnly = true)
    public List<String> getAllEnabledTokens() {
        return deviceTokenRepository.findAllForUsersWithNotificationsEnabled()
                .stream()
                .map(DeviceToken::getToken)
                .toList();
    }

    /** Remove a stale token that FCM has rejected. */
    @Transactional
    public void removeStaleToken(String token) {
        deviceTokenRepository.findByToken(token).ifPresent(t -> {
            deviceTokenRepository.delete(t);
            log.info("[DeviceToken] Removed stale token");
        });
    }
}
