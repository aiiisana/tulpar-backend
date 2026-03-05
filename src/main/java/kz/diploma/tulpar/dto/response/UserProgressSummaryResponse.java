package kz.diploma.tulpar.dto.response;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class UserProgressSummaryResponse {

    private String userId;
    private long totalAttempted;
    private long totalCompleted;
    private long totalFailed;
    private long totalInProgress;
}
