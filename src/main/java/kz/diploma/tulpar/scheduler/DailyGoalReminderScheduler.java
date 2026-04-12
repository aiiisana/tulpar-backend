package kz.diploma.tulpar.scheduler;

import kz.diploma.tulpar.service.SessionTimeService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * Runs once a day at 21:00 (server time).
 * Finds users who have not met their daily study goal and sends them
 * a push notification + in-app notification as a reminder.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class DailyGoalReminderScheduler {

    private final SessionTimeService sessionTimeService;

    /**
     * Every day at 21:00.
     * Cron: second minute hour dayOfMonth month dayOfWeek
     */
    @Scheduled(cron = "0 0 21 * * *")
    public void remindUsersWithUnmetGoals() {
        log.info("[DailyGoalReminder] Starting daily goal check");
        try {
            sessionTimeService.checkAndNotifyGoalMissers();
            log.info("[DailyGoalReminder] Daily goal check completed");
        } catch (Exception e) {
            log.error("[DailyGoalReminder] Error during goal check: {}", e.getMessage(), e);
        }
    }
}
