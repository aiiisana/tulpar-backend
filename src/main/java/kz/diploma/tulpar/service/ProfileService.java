package kz.diploma.tulpar.service;

import kz.diploma.tulpar.domain.entity.User;
import kz.diploma.tulpar.domain.entity.UserStats;
import kz.diploma.tulpar.config.properties.MinioProperties;
import kz.diploma.tulpar.domain.enums.MediaType;
import kz.diploma.tulpar.dto.request.UpdateProfileRequest;
import kz.diploma.tulpar.dto.response.MediaUploadResponse;
import kz.diploma.tulpar.dto.response.ProfileResponse;
import kz.diploma.tulpar.exception.ResourceNotFoundException;
import kz.diploma.tulpar.repository.UserRepository;
import kz.diploma.tulpar.repository.UserStatsRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

@Service
@RequiredArgsConstructor
public class ProfileService {

    private final UserRepository userRepository;
    private final UserStatsRepository userStatsRepository;
    private final MinioService minioService;
    private final MinioProperties minioProperties;

    @Cacheable(value = "user-profile", key = "#uid")
    @Transactional(readOnly = true)
    public ProfileResponse getProfile(String uid) {
        User user = userRepository.findById(uid)
                .orElseThrow(() -> ResourceNotFoundException.of("User", uid));
        UserStats stats = userStatsRepository.findById(uid).orElse(null);
        return toResponse(user, stats);
    }

    @CacheEvict(value = "user-profile", key = "#uid")
    @Transactional
    public ProfileResponse uploadAvatar(String uid, MultipartFile file) {
        String contentType = file.getContentType();
        if (contentType == null || !contentType.startsWith("image/")) {
            throw new IllegalArgumentException("Only image files are allowed for avatar upload");
        }

        MediaUploadResponse uploaded = minioService.upload(file, MediaType.IMAGE, uid);

        User user = userRepository.findById(uid)
                .orElseThrow(() -> ResourceNotFoundException.of("User", uid));
        user.setAvatarObjectKey(uploaded.getObjectKey());
        // avatarUrl is intentionally left as-is; toResponse() computes the fresh URL from objectKey

        UserStats stats = userStatsRepository.findById(uid).orElse(null);
        return toResponse(user, stats);
    }

    @CacheEvict(value = "user-profile", key = "#uid")
    @Transactional
    public ProfileResponse updateProfile(String uid, UpdateProfileRequest req) {
        User user = userRepository.findById(uid)
                .orElseThrow(() -> ResourceNotFoundException.of("User", uid));

        if (req.getUsername() != null)             user.setUsername(req.getUsername());
        if (req.getAvatarUrl() != null)            user.setAvatarUrl(req.getAvatarUrl());
        if (req.getNotificationsEnabled() != null) user.setNotificationsEnabled(req.getNotificationsEnabled());
        if (req.getDailyGoal() != null)            user.setDailyGoal(req.getDailyGoal());

        UserStats stats = userStatsRepository.findById(uid).orElse(null);
        return toResponse(user, stats);
    }

    /**
     * Resolves the avatar URL to return to the client.
     * Priority:
     *  1. If avatarObjectKey is set — generate a fresh URL from R2 (avoids expired/broken stored URLs).
     *  2. Otherwise fall back to the raw avatarUrl (e.g. a Google/Firebase photo URL).
     */
    private String resolveAvatarUrl(User user) {
        if (user.getAvatarObjectKey() != null && !user.getAvatarObjectKey().isBlank()) {
            return minioService.resolveFileUrl(minioProperties.getBucketName(), user.getAvatarObjectKey());
        }
        return user.getAvatarUrl();
    }

    private ProfileResponse toResponse(User user, UserStats stats) {
        return ProfileResponse.builder()
                .id(user.getId())
                .email(user.getEmail())
                .username(user.getUsername())
                .avatarUrl(resolveAvatarUrl(user))
                .role(user.getRole())
                .level(user.getLevel())
                .dailyGoal(user.getDailyGoal())
                .notificationsEnabled(user.isNotificationsEnabled())
                .onboardingCompleted(user.isOnboardingCompleted())
                .createdAt(user.getCreatedAt())
                .currentStreak(stats != null ? stats.getCurrentStreak() : 0)
                .longestStreak(stats != null ? stats.getLongestStreak() : 0)
                .totalXp(stats != null ? stats.getTotalXp() : 0)
                .build();
    }
}
