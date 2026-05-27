package kz.diploma.tulpar.service;

import kz.diploma.tulpar.dto.response.AnalyticsResponse;
import kz.diploma.tulpar.dto.response.AnalyticsResponse.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jakarta.persistence.EntityManager;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AnalyticsService {

    private final EntityManager em;

    @Transactional(readOnly = true)
    public AnalyticsResponse getFullAnalytics() {
        return AnalyticsResponse.builder()
                .dailyActiveUsers(getDailyActiveUsers(30))
                .topErrorExercises(getTopErrorExercises(10))
                .lessonDropoffs(getLessonDropoffs(10))
                .build();
    }

    // ── Daily Active Users ────────────────────────────────────────────────────

    @Transactional(readOnly = true)
    @SuppressWarnings("unchecked")
    public List<DauEntry> getDailyActiveUsers(int days) {
        LocalDate from = LocalDate.now().minusDays(days - 1);
        List<Object[]> rows = em.createQuery(
                "SELECT a.activityDate, COUNT(DISTINCT a.user.id) " +
                "FROM UserDailyActivity a " +
                "WHERE a.activityDate >= :from AND a.completed = true " +
                "GROUP BY a.activityDate " +
                "ORDER BY a.activityDate ASC", Object[].class)
                .setParameter("from", from)
                .getResultList();

        return rows.stream()
                .map(r -> DauEntry.builder()
                        .date((LocalDate) r[0])
                        .activeUsers((Long) r[1])
                        .build())
                .toList();
    }

    // ── Exercise error rates ──────────────────────────────────────────────────

    @Transactional(readOnly = true)
    @SuppressWarnings("unchecked")
    public List<ExerciseErrorStat> getTopErrorExercises(int limit) {
        // Exercises with the highest fail rate (min 5 attempts to filter noise)
        List<Object[]> rows = em.createQuery(
                "SELECT p.exercise.id, p.exercise.question, p.exercise.type, " +
                "       COUNT(p), " +
                "       SUM(CASE WHEN p.status = kz.diploma.tulpar.domain.enums.ProgressStatus.FAILED THEN 1 ELSE 0 END) " +
                "FROM UserProgress p " +
                "GROUP BY p.exercise.id, p.exercise.question, p.exercise.type " +
                "HAVING COUNT(p) >= 5 " +
                "ORDER BY (SUM(CASE WHEN p.status = kz.diploma.tulpar.domain.enums.ProgressStatus.FAILED THEN 1 ELSE 0 END) * 1.0 / COUNT(p)) DESC",
                Object[].class)
                .setMaxResults(limit)
                .getResultList();

        return rows.stream()
                .map(r -> {
                    long total  = ((Number) r[3]).longValue();
                    long failed = r[4] != null ? ((Number) r[4]).longValue() : 0L;
                    int rate    = total > 0 ? (int) Math.round(failed * 100.0 / total) : 0;
                    return ExerciseErrorStat.builder()
                            .exerciseId(r[0].toString())
                            .question((String) r[1])
                            .exerciseType(r[2] != null ? r[2].toString() : "UNKNOWN")
                            .totalAttempts(total)
                            .failedAttempts(failed)
                            .errorRatePercent(rate)
                            .build();
                })
                .toList();
    }

    // ── Lesson drop-off ───────────────────────────────────────────────────────

    @Transactional(readOnly = true)
    @SuppressWarnings("unchecked")
    public List<LessonDropoff> getLessonDropoffs(int limit) {
        // For each lesson count how many distinct users started vs completed
        List<Object[]> rows = em.createQuery(
                "SELECT le.lesson.id, le.lesson.title, " +
                "       COUNT(DISTINCT p.user.id) as started " +
                "FROM LessonExercise le " +
                "JOIN UserProgress p ON p.exercise.id = le.exercise.id " +
                "GROUP BY le.lesson.id, le.lesson.title " +
                "ORDER BY started DESC", Object[].class)
                .setMaxResults(limit)
                .getResultList();

        List<LessonDropoff> result = new ArrayList<>();
        for (Object[] r : rows) {
            String lessonId    = r[0].toString();
            String lessonTitle = (String) r[1];
            long started       = ((Number) r[2]).longValue();

            // Count users who completed ALL exercises in this lesson
            Long completed = (Long) em.createQuery(
                    "SELECT COUNT(DISTINCT p.user.id) FROM LessonExercise le " +
                    "JOIN UserProgress p ON p.exercise.id = le.exercise.id " +
                    "WHERE le.lesson.id = :lid " +
                    "  AND p.status = kz.diploma.tulpar.domain.enums.ProgressStatus.COMPLETED " +
                    "GROUP BY p.user.id " +
                    "HAVING COUNT(p) = (SELECT COUNT(le2) FROM LessonExercise le2 WHERE le2.lesson.id = :lid)")
                    .setParameter("lid", java.util.UUID.fromString(lessonId))
                    .getResultStream()
                    .count();

            int dropoff = started > 0
                    ? (int) Math.round((started - completed) * 100.0 / started) : 0;

            result.add(LessonDropoff.builder()
                    .lessonId(lessonId)
                    .lessonTitle(lessonTitle)
                    .startedCount(started)
                    .completedCount(completed)
                    .dropoffPercent(dropoff)
                    .build());
        }
        return result;
    }
}
