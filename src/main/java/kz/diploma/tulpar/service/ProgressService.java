package kz.diploma.tulpar.service;

import kz.diploma.tulpar.domain.entity.Exercise;
import kz.diploma.tulpar.domain.entity.User;
import kz.diploma.tulpar.domain.entity.UserProgress;
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
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

@Service
@RequiredArgsConstructor
public class ProgressService {

    private final UserProgressRepository progressRepository;
    private final UserRepository userRepository;
    private final ExerciseRepository exerciseRepository;

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
        return toResponse(saved, correct);
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

    // ─── Answer evaluation ───────────────────────────────────────────────────

    private boolean evaluate(Exercise exercise, String userAnswer) {
        return switch (exercise) {
            case VocabularyExercise v -> v.getCorrectAnswer().equalsIgnoreCase(userAnswer.trim());
            case ListeningExercise l  -> l.getCorrectAnswer().equalsIgnoreCase(userAnswer.trim());
            case VideoExercise v      -> v.getCorrectAnswer().equalsIgnoreCase(userAnswer.trim());
            case ImageExercise i      -> i.getCorrectAnswer().equalsIgnoreCase(userAnswer.trim());
            case SentenceBuilderExercise s ->
                    s.getCorrectSentence().equalsIgnoreCase(userAnswer.trim());
            default -> false;
        };
    }

    private ProgressResponse toResponse(UserProgress p, boolean correct) {
        return ProgressResponse.builder()
                .progressId(p.getId())
                .exerciseId(p.getExercise().getId())
                .userId(p.getUser().getId())
                .status(p.getStatus())
                .attempts(p.getAttempts())
                .correct(correct)
                .completedAt(p.getCompletedAt())
                .lastAttemptedAt(p.getLastAttemptedAt())
                .build();
    }
}
