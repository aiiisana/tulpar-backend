package kz.diploma.tulpar.service;

import kz.diploma.tulpar.domain.entity.User;
import kz.diploma.tulpar.domain.entity.UserStats;
import kz.diploma.tulpar.dto.request.UpdateProfileRequest;
import kz.diploma.tulpar.dto.response.ProfileResponse;
import kz.diploma.tulpar.exception.ResourceNotFoundException;
import kz.diploma.tulpar.repository.UserRepository;
import kz.diploma.tulpar.repository.UserStatsRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ProfileService {

    private final UserRepository userRepository;
    private final UserStatsRepository userStatsRepository;

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
    public ProfileResponse updateProfile(String uid, UpdateProfileRequest req) {
        User user = userRepository.findById(uid)
                .orElseThrow(() -> ResourceNotFoundException.of("User", uid));

        if (req.getUsername() != null)             user.setUsername(req.getUsername());
        if (req.getAvatarUrl() != null)            user.setAvatarUrl(req.getAvatarUrl());
        if (req.getNotificationsEnabled() != null) user.setNotificationsEnabled(req.getNotificationsEnabled());

        UserStats stats = userStatsRepository.findById(uid).orElse(null);
        return toResponse(user, stats);
    }

    private ProfileResponse toResponse(User user, UserStats stats) {
        return ProfileResponse.builder()
                .id(user.getId())
                .email(user.getEmail())
                .username(user.getUsername())
                .avatarUrl(user.getAvatarUrl())
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
