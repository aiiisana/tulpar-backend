package kz.diploma.tulpar.service;

import kz.diploma.tulpar.domain.entity.Exercise;
import kz.diploma.tulpar.domain.entity.SpacedRepetitionState;
import kz.diploma.tulpar.dto.request.SrsAnswerRequest;
import kz.diploma.tulpar.dto.response.SrsCardResponse;
import kz.diploma.tulpar.exception.ResourceNotFoundException;
import kz.diploma.tulpar.repository.ExerciseRepository;
import kz.diploma.tulpar.repository.SpacedRepetitionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.UUID;

/**
 * Implements the SM-2 spaced-repetition algorithm.
 *
 * Quality mapping used here:
 *   correct  → quality 4  (correct after some hesitation)
 *   incorrect → quality 1 (incorrect, answer recalled on seeing it)
 *
 * SM-2 reference: https://www.supermemo.com/en/archives1990-2015/english/ol/sm2
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class SpacedRepetitionService {

    private static final double INITIAL_EASE   = 2.5;
    private static final double MIN_EASE       = 1.3;
    private static final int    MAX_DUE_CARDS  = 50;

    private final SpacedRepetitionRepository srsRepo;
    private final ExerciseRepository exerciseRepo;
    private final ExerciseService exerciseService;

    // ─── Public API ───────────────────────────────────────────────────────────

    /**
     * Returns exercises that are due for review (next_review_at ≤ now),
     * ordered by most overdue first, capped at MAX_DUE_CARDS.
     */
    @Transactional(readOnly = true)
    public List<SrsCardResponse> getDueCards(String userId) {
        List<SpacedRepetitionState> due = srsRepo.findDueCards(
                userId, Instant.now(), PageRequest.of(0, MAX_DUE_CARDS));

        return due.stream()
                .map(state -> SrsCardResponse.builder()
                        .exercise(exerciseService.toResponse(state.getExercise()))
                        .repetitions(state.getRepetitions())
                        .intervalDays(state.getIntervalDays())
                        .dueAt(state.getNextReviewAt())
                        .build())
                .toList();
    }

    /** Count of due cards (used by the home screen badge). */
    @Transactional(readOnly = true)
    public long countDue(String userId) {
        return srsRepo.countByUserIdAndNextReviewAtBefore(userId, Instant.now());
    }

    /**
     * Records the result of one review and advances the SM-2 state.
     * Called after each exercise in the review session.
     * Also called implicitly from ProgressService after every lesson attempt
     * to seed the SRS state for newly-seen exercises.
     */
    @Transactional
    public void recordAnswer(String userId, SrsAnswerRequest req) {
        Exercise exercise = exerciseRepo.findById(req.getExerciseId())
                .orElseThrow(() -> ResourceNotFoundException.of("Exercise", req.getExerciseId()));

        SpacedRepetitionState state = srsRepo
                .findByUserIdAndExerciseId(userId, req.getExerciseId())
                .orElseGet(() -> SpacedRepetitionState.builder()
                        .userId(userId)
                        .exercise(exercise)
                        .easeFactor(INITIAL_EASE)
                        .intervalDays(1)
                        .repetitions(0)
                        .nextReviewAt(Instant.now())
                        .build());

        applySm2(state, req.getCorrect());
        srsRepo.save(state);
        log.debug("[SRS] user={} exercise={} correct={} → interval={}d ease={}",
                userId, req.getExerciseId(), req.getCorrect(),
                state.getIntervalDays(), state.getEaseFactor());
    }

    /**
     * Seeds or resets SRS state after a first-time lesson answer.
     * Does NOT overwrite existing state if the card has already been reviewed.
     */
    @Transactional
    public void seedFromLesson(String userId, UUID exerciseId, boolean correct) {
        Exercise exercise = exerciseRepo.findById(exerciseId)
                .orElseThrow(() -> ResourceNotFoundException.of("Exercise", exerciseId));

        // Only create/update if not yet seeded or if repetitions == 0 (never properly reviewed)
        SpacedRepetitionState state = srsRepo
                .findByUserIdAndExerciseId(userId, exerciseId)
                .orElseGet(() -> SpacedRepetitionState.builder()
                        .userId(userId)
                        .exercise(exercise)
                        .easeFactor(INITIAL_EASE)
                        .intervalDays(1)
                        .repetitions(0)
                        .nextReviewAt(Instant.now())
                        .build());

        // Skip if already reviewed at least once (let SRS manage it independently)
        if (state.getRepetitions() > 0) return;

        applySm2(state, correct);
        srsRepo.save(state);
    }

    // ─── SM-2 core ────────────────────────────────────────────────────────────

    private void applySm2(SpacedRepetitionState state, boolean correct) {
        // Map correct/incorrect to SM-2 quality score
        int quality = correct ? 4 : 1;

        if (correct) {
            int rep = state.getRepetitions() + 1;
            int interval = switch (rep) {
                case 1 -> 1;
                case 2 -> 6;
                default -> (int) Math.round(state.getIntervalDays() * state.getEaseFactor());
            };

            // Update ease factor: EF' = EF + (0.1 - (5-q)*(0.08 + (5-q)*0.02))
            double easeDelta = 0.1 - (5.0 - quality) * (0.08 + (5.0 - quality) * 0.02);
            double newEase = Math.max(MIN_EASE, state.getEaseFactor() + easeDelta);

            state.setRepetitions(rep);
            state.setIntervalDays(interval);
            state.setEaseFactor(newEase);
            state.setNextReviewAt(Instant.now().plus(interval, ChronoUnit.DAYS));
        } else {
            // Wrong answer — reset repetitions, review again soon
            state.setRepetitions(0);
            state.setIntervalDays(1);
            // Penalise ease factor (but do not drop below minimum)
            double penalised = Math.max(MIN_EASE, state.getEaseFactor() - 0.2);
            state.setEaseFactor(penalised);
            state.setNextReviewAt(Instant.now().plus(10, ChronoUnit.MINUTES));
        }
    }
}
