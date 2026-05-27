package kz.diploma.tulpar.service;

import kz.diploma.tulpar.domain.entity.User;
import kz.diploma.tulpar.domain.entity.UserDailyActivity;
import kz.diploma.tulpar.domain.entity.UserStats;
import kz.diploma.tulpar.dto.response.CalendarDayResponse;
import kz.diploma.tulpar.dto.response.StatsResponse;
import kz.diploma.tulpar.exception.ResourceNotFoundException;
import kz.diploma.tulpar.repository.UserDailyActivityRepository;
import kz.diploma.tulpar.repository.UserRepository;
import kz.diploma.tulpar.repository.UserStatsRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.IntStream;

@Service
@RequiredArgsConstructor
public class StreakService {

    private final UserStatsRepository userStatsRepository;
    private final UserDailyActivityRepository activityRepository;
    private final UserRepository userRepository;
    private final AchievementService achievementService;

    @Transactional(readOnly = true)
    public StatsResponse getStats(String userId) {
        UserStats stats = getOrCreateStats(userId);
        return StatsResponse.builder()
                .currentStreak(stats.getCurrentStreak())
                .longestStreak(stats.getLongestStreak())
                .totalXp(stats.getTotalXp())
                .lastActivityDate(stats.getLastActivityDate())
                .build();
    }

    @Transactional(readOnly = true)
    public List<CalendarDayResponse> getWeeklyCalendar(String userId) {
        LocalDate today = LocalDate.now();
        LocalDate weekStart = today.minusDays(6);

        List<UserDailyActivity> activities = activityRepository
                .findAllByUserIdAndActivityDateBetweenOrderByActivityDateAsc(userId, weekStart, today);

        return IntStream.rangeClosed(0, 6)
                .mapToObj(i -> {
                    LocalDate date = weekStart.plusDays(i);
                    boolean completed = activities.stream()
                            .anyMatch(a -> a.getActivityDate().equals(date) && a.isCompleted());
                    return CalendarDayResponse.builder().date(date).completed(completed).build();
                })
                .toList();
    }

    @Transactional
    public void recordActivity(String userId) {
        LocalDate today = LocalDate.now();

        // Upsert daily activity
        UserDailyActivity activity = activityRepository
                .findByUserIdAndActivityDate(userId, today)
                .orElseGet(() -> {
                    User user = userRepository.getReferenceById(userId);
                    return UserDailyActivity.builder()
                            .user(user)
                            .activityDate(today)
                            .build();
                });
        activity.setCompleted(true);
        activityRepository.save(activity);

        // Update streak
        UserStats stats = getOrCreateStats(userId);
        LocalDate last = stats.getLastActivityDate();

        if (last == null || last.isBefore(today.minusDays(1))) {
            // streak broken or first activity
            stats.setCurrentStreak(1);
        } else if (last.equals(today.minusDays(1))) {
            // consecutive day
            stats.setCurrentStreak(stats.getCurrentStreak() + 1);
        }
        // same day — no change to streak count

        stats.setLastActivityDate(today);
        if (stats.getCurrentStreak() > stats.getLongestStreak()) {
            stats.setLongestStreak(stats.getCurrentStreak());
        }
        userStatsRepository.save(stats);
        // Check streak achievements after saving
        achievementService.checkStreakAchievements(userId, stats.getCurrentStreak());
    }

    @Transactional
    public void addXp(String userId, int xp) {
        UserStats stats = getOrCreateStats(userId);
        stats.setTotalXp(stats.getTotalXp() + xp);
        userStatsRepository.save(stats);
    }

    /**
     * Records activity AND awards XP in a single transaction to avoid
     * the double-load of UserStats that would occur if called separately.
     */
    @Transactional
    public void recordActivityAndAddXp(String userId, int xp) {
        recordActivity(userId);
        // At this point stats are already loaded in the persistence context;
        // addXp will reuse the managed entity via first-level cache.
        addXp(userId, xp);
    }

    private UserStats getOrCreateStats(String userId) {
        return userStatsRepository.findById(userId).orElseGet(() -> {
            User user = userRepository.getReferenceById(userId);
            return userStatsRepository.save(UserStats.builder().user(user).build());
        });
    }
}
