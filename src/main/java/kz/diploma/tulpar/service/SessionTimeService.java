package kz.diploma.tulpar.service;

import kz.diploma.tulpar.domain.entity.User;
import kz.diploma.tulpar.domain.entity.UserSessionTime;
import kz.diploma.tulpar.domain.enums.DailyGoal;
import kz.diploma.tulpar.dto.response.SessionTimeResponse;
import kz.diploma.tulpar.exception.ResourceNotFoundException;
import kz.diploma.tulpar.repository.UserRepository;
import kz.diploma.tulpar.repository.UserSessionTimeRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class SessionTimeService {

    private final UserSessionTimeRepository sessionTimeRepository;
    private final UserRepository userRepository;
    private final NotificationService notificationService;

    // ── Public API ─────────────────────────────────────────────────────────────

    /**
     * Add seconds to the user's today's session time.
     * Uses a native upsert so concurrent flushes are safe.
     *
     * @return updated today's session time response
     */
    @Transactional
    public SessionTimeResponse addSeconds(String userId, int seconds) {
        if (seconds <= 0) {
            return getTodayTime(userId);
        }
        sessionTimeRepository.upsertAddSeconds(userId, LocalDate.now(), seconds);
        return getTodayTime(userId);
    }

    /**
     * Returns today's cumulative session time + goal info for the user.
     */
    @Transactional(readOnly = true)
    public SessionTimeResponse getTodayTime(String userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> ResourceNotFoundException.of("User", userId));

        int goalSeconds = toSeconds(user.getDailyGoal());

        Optional<UserSessionTime> record =
                sessionTimeRepository.findByUserIdAndSessionDate(userId, LocalDate.now());

        int totalSeconds = record.map(UserSessionTime::getTotalSeconds).orElse(0);

        return SessionTimeResponse.builder()
                .totalSeconds(totalSeconds)
                .goalSeconds(goalSeconds)
                .goalMet(goalSeconds > 0 && totalSeconds >= goalSeconds)
                .build();
    }

    /**
     * Called by the daily scheduler at 21:00.
     * Finds all users who have a daily goal set but haven't met it today,
     * and sends them a push notification.
     */
    @Transactional(readOnly = true)
    public void checkAndNotifyGoalMissers() {
        LocalDate today = LocalDate.now();
        log.info("[SessionTime] Running daily goal check for {}", today);

        List<User> usersWithGoal = userRepository.findAllByDailyGoalIsNotNullAndNotificationsEnabledTrue();

        for (User user : usersWithGoal) {
            int goalSeconds = toSeconds(user.getDailyGoal());
            if (goalSeconds <= 0) continue;

            int spentSeconds = sessionTimeRepository
                    .findByUserIdAndSessionDate(user.getId(), today)
                    .map(UserSessionTime::getTotalSeconds)
                    .orElse(0);

            if (spentSeconds < goalSeconds) {
                int remainingMin = (goalSeconds - spentSeconds) / 60;
                String title = "Не забудь про казахский!";
                String body = remainingMin > 1
                        ? "До выполнения дневной цели осталось всего %d мин. Ты справишься!".formatted(remainingMin)
                        : "До выполнения дневной цели осталась всего 1 мин. Не сдавайся!";

                try {
                    notificationService.createAndPush(user.getId(), title, body);
                    log.debug("[SessionTime] Notified user={} spent={}s goal={}s", user.getId(), spentSeconds, goalSeconds);
                } catch (Exception e) {
                    log.warn("[SessionTime] Failed to notify user={}: {}", user.getId(), e.getMessage());
                }
            }
        }
    }

    // ── Helpers ────────────────────────────────────────────────────────────────

    /** Convert DailyGoal enum to seconds. */
    public static int toSeconds(DailyGoal goal) {
        if (goal == null) return 0;
        return switch (goal) {
            case CASUAL  ->  5 * 60;   //  5 min
            case REGULAR -> 10 * 60;   // 10 min
            case SERIOUS -> 20 * 60;   // 20 min
            case INTENSE -> 30 * 60;   // 30 min
        };
    }
}
