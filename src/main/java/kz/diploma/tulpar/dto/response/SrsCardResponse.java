package kz.diploma.tulpar.dto.response;

import lombok.Builder;
import lombok.Data;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

import java.time.Instant;

/**
 * One SRS review card — wraps the exercise with metadata about the current
 * SM-2 state (interval, repetitions) so the client can display progress info.
 */
@Data
@Builder @NoArgsConstructor @AllArgsConstructor
public class SrsCardResponse {

    private ExerciseResponse exercise;

    /** Current SM-2 repetition count (≥1 means it has been seen before). */
    private int repetitions;

    /** Current interval in days. */
    private int intervalDays;

    /** Timestamp the card was due. */
    private Instant dueAt;
}
