package kz.diploma.tulpar.dto.response;

import kz.diploma.tulpar.domain.enums.DifficultyLevel;
import kz.diploma.tulpar.domain.enums.ExerciseType;
import lombok.Builder;
import lombok.Data;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

/**
 * Unified exercise response sent to the Flutter client.
 * Only the fields relevant to the exercise type are populated; the rest are null.
 * This avoids polymorphic JSON complexity on the client side.
 */
@Data
@Builder
public class ExerciseResponse {

    private UUID id;
    private ExerciseType type;
    private DifficultyLevel difficultyLevel;
    private String question;
    private String explanation;
    private Instant createdAt;

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

    // ── Shared (Vocabulary / Listening / Video / Image) ───────────────────────
    /** Answer choices shown to the user. correctAnswer is NOT included here. */
    private List<String> options;

    // ── Sentence builder ──────────────────────────────────────────────────────
    private List<String> shuffledWords;
}
