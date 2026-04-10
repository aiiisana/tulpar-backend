package kz.diploma.tulpar.dto.response;

import kz.diploma.tulpar.domain.enums.ProgressStatus;
import lombok.Builder;
import lombok.Data;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.UUID;

@Data
@Builder @NoArgsConstructor @AllArgsConstructor
public class ProgressResponse {

    private UUID progressId;
    private UUID exerciseId;
    private String userId;
    private ProgressStatus status;
    private int attempts;
    private boolean correct;
    /** XP awarded for this submission (0 when wrong, positive when correct). */
    private int xpEarned;
    private Instant completedAt;
    private Instant lastAttemptedAt;
}
