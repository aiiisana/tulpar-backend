package kz.diploma.tulpar.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * Scheduled push notification reminders.
 *
 * Times are in UTC — adjust the cron expressions for your target timezone
 * or configure spring.task.scheduling.pool.size in application.yml.
 *
 * Daily lesson reminder  — 09:00 UTC (12:00 Almaty, UTC+5)
 * Evening streak nudge   — 17:00 UTC (22:00 Almaty) — only if user has a streak
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class ReminderScheduler {

    private final NotificationService notificationService;

    /**
     * Morning reminder: sent every day at 09:00 UTC.
     * Broadcasts to all users who have notifications enabled.
     */
    @Scheduled(cron = "0 0 9 * * *")
    public void sendMorningReminder() {
        log.info("[Scheduler] Sending morning lesson reminder");
        notificationService.broadcastPush(
                "Сабақ уақыты! 📚",
                "Бүгінгі казахша сабақты бастауға дайынсыз ба?"
        );
    }

    /**
     * Evening streak nudge: sent every day at 17:00 UTC.
     * Remind users who may not have studied yet today.
     */
    @Scheduled(cron = "0 0 17 * * *")
    public void sendEveningReminder() {
        log.info("[Scheduler] Sending evening streak reminder");
        notificationService.broadcastPush(
                "Стригіңізді үзбеңіз! 🔥",
                "Бүгін сабаққа кірдіңіз бе? Бір сабақ ғана жеткілікті!"
        );
    }
}
