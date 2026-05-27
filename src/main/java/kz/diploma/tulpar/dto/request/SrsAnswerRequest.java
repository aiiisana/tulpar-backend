package kz.diploma.tulpar.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.UUID;

/**
 * Answer submitted during an SRS review session.
 * {@code correct} is sent by the client after the user reveals the answer and
 * self-evaluates (flashcard-style) OR after the server validates a typed answer.
 */
@Data
public class SrsAnswerRequest {

    @NotNull
    private UUID exerciseId;

    /** True if the user got the answer right. */
    @NotNull
    private Boolean correct;
}
