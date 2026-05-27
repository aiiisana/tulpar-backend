package kz.diploma.tulpar.dto.response;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;
import java.util.List;

@Data
@Builder
public class AnalyticsResponse {

    private List<DauEntry>         dailyActiveUsers;   // last 30 days
    private List<ExerciseErrorStat> topErrorExercises; // top 10 by error rate
    private List<LessonDropoff>    lessonDropoffs;     // lessons with most incomplete attempts

    @Data @Builder
    public static class DauEntry {
        private LocalDate date;
        private long activeUsers;
    }

    @Data @Builder
    public static class ExerciseErrorStat {
        private String exerciseId;
        private String question;
        private String exerciseType;
        private long totalAttempts;
        private long failedAttempts;
        private int errorRatePercent;
    }

    @Data @Builder
    public static class LessonDropoff {
        private String lessonId;
        private String lessonTitle;
        private long startedCount;   // users who have at least 1 progress entry
        private long completedCount; // users who passed the 90% threshold
        private int dropoffPercent;
    }
}
