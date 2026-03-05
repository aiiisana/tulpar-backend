package kz.diploma.tulpar.dto.response;

import kz.diploma.tulpar.domain.enums.ProgressStatus;
import lombok.Builder;
import lombok.Data;

import java.time.Instant;
import java.util.UUID;

@Data
@Builder
public class ProgressResponse {

    private UUID progressId;
    private UUID exerciseId;
    private String userId;
    private ProgressStatus status;
    private int attempts;
    private boolean correct;
    private Instant completedAt;
    private Instant lastAttemptedAt;
}
