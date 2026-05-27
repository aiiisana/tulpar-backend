package kz.diploma.tulpar.service;

import kz.diploma.tulpar.domain.entity.Achievement;
import kz.diploma.tulpar.domain.entity.User;
import kz.diploma.tulpar.domain.entity.UserAchievement;
import kz.diploma.tulpar.dto.response.AchievementResponse;
import kz.diploma.tulpar.repository.AchievementRepository;
import kz.diploma.tulpar.repository.UserAchievementRepository;
import kz.diploma.tulpar.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class AchievementService {

    private final AchievementRepository achievementRepository;
    private final UserAchievementRepository userAchievementRepository;
    private final UserRepository userRepository;

    /**
     * Returns ALL achievements with earned status for the given user.
     * Unearned achievements are included so the client can show locked badges.
     */
    @Transactional(readOnly = true)
    public List<AchievementResponse> getForUser(String userId) {
        List<Achievement> all = achievementRepository.findAllByOrderBySortOrderAsc();
        List<UserAchievement> earned = userAchievementRepository
                .findAllByUserIdOrderByEarnedAtDesc(userId);

        Set<String> earnedCodes = earned.stream()
                .map(UserAchievement::getAchievementCode)
                .collect(Collectors.toSet());

        java.util.Map<String, UserAchievement> earnedMap = earned.stream()
                .collect(Collectors.toMap(
                        UserAchievement::getAchievementCode, ua -> ua));

        return all.stream()
                .map(a -> AchievementResponse.builder()
                        .id(a.getId())
                        .code(a.getCode())
                        .title(a.getTitle())
                        .description(a.getDescription())
                        .iconName(a.getIconName())
                        .xpReward(a.getXpReward())
                        .earned(earnedCodes.contains(a.getCode()))
                        .earnedAt(earnedCodes.contains(a.getCode())
                                ? earnedMap.get(a.getCode()).getEarnedAt()
                                : null)
                        .build())
                .toList();
    }

    /**
     * Awards an achievement to the user if they haven't earned it yet.
     * Idempotent — safe to call multiple times.
     *
     * @return true if the achievement was newly awarded, false if already had it
     */
    @Transactional
    public boolean award(String userId, String code) {
        if (userAchievementRepository.existsByUserIdAndAchievementCode(userId, code)) {
            return false; // already earned — no-op
        }
        achievementRepository.findByCode(code).ifPresent(achievement -> {
            User user = userRepository.getReferenceById(userId);
            userAchievementRepository.save(UserAchievement.builder()
                    .user(user)
                    .achievementCode(code)
                    .build());
            log.info("Achievement '{}' awarded to user {}", code, userId);
        });
        return true;
    }

    /**
     * Called after any lesson or exercise completion. Checks and awards
     * word/lesson-related achievements based on total completed exercise count.
     */
    @Transactional
    public void checkProgressAchievements(String userId, long totalCompletedExercises) {
        if (totalCompletedExercises >= 10)  award(userId, "WORDS_10");
        if (totalCompletedExercises >= 50)  award(userId, "WORDS_50");
        if (totalCompletedExercises >= 100) award(userId, "WORDS_100");
        if (totalCompletedExercises >= 1)   award(userId, "FIRST_LESSON");
    }

    /**
     * Called after streak update. Checks streak-based achievements.
     */
    @Transactional
    public void checkStreakAchievements(String userId, int currentStreak) {
        if (currentStreak >= 3)  award(userId, "STREAK_3");
        if (currentStreak >= 7)  award(userId, "STREAK_7");
        if (currentStreak >= 30) award(userId, "STREAK_30");
    }

    /**
     * Called after XP update. Checks XP milestone achievements.
     */
    @Transactional
    public void checkXpAchievements(String userId, long totalXp) {
        if (totalXp >= 500)  award(userId, "XP_500");
        if (totalXp >= 1000) award(userId, "XP_1000");
    }

    /**
     * Called after daily challenge completion.
     */
    @Transactional
    public void checkDailyAchievements(String userId, int dailyChallengeStreak) {
        award(userId, "FIRST_DAILY");
        if (dailyChallengeStreak >= 7) award(userId, "DAILY_7");
    }
}
