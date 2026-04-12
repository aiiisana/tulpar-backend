package kz.diploma.tulpar.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.UUID;

@Data
public class SubmitDailyChallengeRequest {

    @NotNull(message = "challengeId is required")
    private UUID challengeId;

    /** The user's assembled answer (case-insensitive comparison on backend). */
    @NotBlank(message = "answer must not be blank")
    private String answer;
}
