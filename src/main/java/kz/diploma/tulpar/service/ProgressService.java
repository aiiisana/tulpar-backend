package kz.diploma.tulpar.service;

import kz.diploma.tulpar.domain.entity.*;
import kz.diploma.tulpar.domain.enums.ProgressStatus;
import kz.diploma.tulpar.dto.request.SubmitProgressRequest;
import kz.diploma.tulpar.dto.response.PageResponse;
import kz.diploma.tulpar.dto.response.ProgressResponse;
import kz.diploma.tulpar.dto.response.UserProgressSummaryResponse;
import kz.diploma.tulpar.exception.ResourceNotFoundException;
import kz.diploma.tulpar.repository.ExerciseRepository;
import kz.diploma.tulpar.repository.UserProgressRepository;
import kz.diploma.tulpar.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

@Slf4j
@Service
@RequiredArgsConstructor
public class ProgressService {

    private final UserProgressRepository progressRepository;
    private final UserRepository userRepository;
    private final ExerciseRepository exerciseRepository;
    private final StreakService streakService;
    private final AchievementService achievementService;
    private final SpacedRepetitionService spacedRepetitionService;

    /**
     * Records or updates a user's attempt on an exercise.
     * Answer correctness is validated server-side.
     */
    @CacheEvict(value = "user-progress", key = "#userId")
    @Transactional
    public ProgressResponse submit(String userId, SubmitProgressRequest req) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> ResourceNotFoundException.of("User", userId));
        Exercise exercise = exerciseRepository.findById(req.getExerciseId())
                .orElseThrow(() -> ResourceNotFoundException.of("Exercise", req.getExerciseId()));

        boolean correct = evaluate(exercise, req.getUserAnswer());
        ProgressStatus status = correct ? ProgressStatus.COMPLETED : ProgressStatus.FAILED;

        UserProgress progress = progressRepository
                .findByUserIdAndExerciseId(userId, req.getExerciseId())
                .orElseGet(() -> UserProgress.builder()
                        .user(user)
                        .exercise(exercise)
                        .attempts(0)
                        .status(ProgressStatus.IN_PROGRESS)
                        .build());

        progress.setAttempts(progress.getAttempts() + 1);
        progress.setStatus(status);
        if (correct && progress.getCompletedAt() == null) {
            progress.setCompletedAt(Instant.now());
        }

        UserProgress saved = progressRepository.save(progress);

        int xpEarned = 0;
        if (correct) {
            xpEarned = 5;
            streakService.recordActivityAndAddXp(userId, xpEarned);
            // Check achievement milestones after awarding XP
            long totalCompleted = progressRepository
                    .countByUserIdAndStatus(userId, ProgressStatus.COMPLETED);
            achievementService.checkProgressAchievements(userId, totalCompleted);
            long totalXp = streakService.getStats(userId).getTotalXp();
            achievementService.checkXpAchievements(userId, totalXp);
        }

        // Seed/update SRS state — done outside of the correctness guard so
        // wrong answers also get the card scheduled for review soon.
        spacedRepetitionService.seedFromLesson(userId, req.getExerciseId(), correct);

        String correctAnswer = extractCorrectAnswer(exercise);
        return toResponse(saved, correct, xpEarned, correctAnswer);
    }

    @Cacheable(value = "user-progress", key = "#userId")
    @Transactional(readOnly = true)
    public PageResponse<ProgressResponse> findByUser(String userId, int page, int size) {
        var pageable = PageRequest.of(page, size, Sort.by("lastAttemptedAt").descending());
        return PageResponse.from(
                progressRepository.findAllByUserId(userId, pageable)
                        .map(p -> toResponse(p, p.getStatus() == ProgressStatus.COMPLETED))
        );
    }

    @Transactional(readOnly = true)
    public UserProgressSummaryResponse getSummary(String userId) {
        long completed  = progressRepository.countByUserIdAndStatus(userId, ProgressStatus.COMPLETED);
        long failed     = progressRepository.countByUserIdAndStatus(userId, ProgressStatus.FAILED);
        long inProgress = progressRepository.countByUserIdAndStatus(userId, ProgressStatus.IN_PROGRESS);
        return UserProgressSummaryResponse.builder()
                .userId(userId)
                .totalAttempted(completed + failed + inProgress)
                .totalCompleted(completed)
                .totalFailed(failed)
                .totalInProgress(inProgress)
                .build();
    }

    // ─── Correct answer extraction ────────────────────────────────────────────

    /**
     * Returns the canonical correct answer for an exercise so the client can
     * display it in the feedback bar after a wrong submission.
     *
     * This is intentionally called only from {@link #submit} — the correct
     * answer is never included in GET /exercises responses so users cannot
     * peek before answering.
     *
     * Returns {@code null} for exercise types that have no deterministic
     * correct answer (e.g. AI_GENERATED without a concrete entity subclass).
     */
    private String extractCorrectAnswer(Exercise exercise) {
        return switch (exercise) {
            case VocabularyExercise v       -> v.getCorrectAnswer();
            case ListeningExercise  l       -> l.getCorrectAnswer();
            case VideoExercise      v       -> v.getCorrectAnswer();
            case ImageExercise      i       -> i.getCorrectAnswer();
            case SentenceBuilderExercise s  -> s.getCorrectSentence();
            default                         -> null;
        };
    }

    // ─── Answer evaluation ───────────────────────────────────────────────────

    private boolean evaluate(Exercise exercise, String userAnswer) {
        return switch (exercise) {
            case VocabularyExercise v -> v.getCorrectAnswer().equalsIgnoreCase(userAnswer.trim());
            case ListeningExercise l  -> l.getCorrectAnswer().equalsIgnoreCase(userAnswer.trim());
            case VideoExercise v      -> v.getCorrectAnswer().equalsIgnoreCase(userAnswer.trim());
            case ImageExercise i      -> i.getCorrectAnswer().equalsIgnoreCase(userAnswer.trim());
            case SentenceBuilderExercise s ->
                    s.getCorrectSentence().equalsIgnoreCase(userAnswer.trim());
            default -> {
                // AI_GENERATED and any future exercise types without a concrete entity class
                // land here. Log a clear warning so developers know evaluation is skipped.
                log.warn("evaluate(): unsupported exercise class '{}' (id={}). " +
                         "Returning false. Implement a dedicated entity subclass to enable scoring.",
                         exercise.getClass().getSimpleName(), exercise.getId());
                yield false;
            }
        };
    }

    /**
     * Full overload — used by {@link #submit} which knows XP and the correct answer.
     * {@code correctAnswer} may be {@code null} for exercise types where it cannot
     * be extracted (e.g. AI_GENERATED).
     */
    private ProgressResponse toResponse(UserProgress p, boolean correct,
                                        int xpEarned, String correctAnswer) {
        return ProgressResponse.builder()
                .progressId(p.getId())
                .exerciseId(p.getExercise().getId())
                .userId(p.getUser().getId())
                .status(p.getStatus())
                .attempts(p.getAttempts())
                .correct(correct)
                .correctAnswer(correctAnswer)
                .xpEarned(xpEarned)
                .completedAt(p.getCompletedAt())
                .lastAttemptedAt(p.getLastAttemptedAt())
                .build();
    }

    /** Overload used by findByUser() — XP and correctAnswer not re-calculated from history. */
    private ProgressResponse toResponse(UserProgress p, boolean correct) {
        return toResponse(p, correct, 0, null);
    }
}
