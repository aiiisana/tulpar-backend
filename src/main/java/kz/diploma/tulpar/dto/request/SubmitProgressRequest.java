package kz.diploma.tulpar.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.UUID;

/**
 * Submitted by the Flutter client after the user answers an exercise.
 */
@Data
public class SubmitProgressRequest {

    @NotNull
    private UUID exerciseId;

    /**
     * The answer string the user submitted.
     * For sentence builder, this is the sentence joined by spaces.
     */
    @NotBlank
    private String userAnswer;
}
