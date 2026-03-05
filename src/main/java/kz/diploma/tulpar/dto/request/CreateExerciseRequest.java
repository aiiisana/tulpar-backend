package kz.diploma.tulpar.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import kz.diploma.tulpar.domain.enums.DifficultyLevel;
import kz.diploma.tulpar.domain.enums.ExerciseType;
import lombok.Data;

import java.util.List;

/**
 * Unified create request for all exercise types.
 * Fields irrelevant to a particular type are simply left null.
 */
@Data
public class CreateExerciseRequest {

    @NotNull
    private ExerciseType type;

    @NotNull
    private DifficultyLevel difficultyLevel;

    @NotBlank
    private String question;

    private String explanation;

    // ── Vocabulary ────────────────────────────────────────────────────────────
    private String word;
    private String translation;

    // ── Listening ─────────────────────────────────────────────────────────────
    private String audioUrl;
    private String transcript;

    // ── Video ─────────────────────────────────────────────────────────────────
    private String videoUrl;
    private Double startTime;
    private Double endTime;

    // ── Image ─────────────────────────────────────────────────────────────────
    private String imageUrl;

    // ── Vocabulary / Listening / Video / Image (shared) ──────────────────────
    private List<String> options;
    private String correctAnswer;

    // ── Sentence builder ──────────────────────────────────────────────────────
    private String correctSentence;
    private List<String> shuffledWords;
}
