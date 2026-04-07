package kz.diploma.tulpar.dto.response;

import lombok.Builder;
import lombok.Data;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

@Data
@Builder @NoArgsConstructor @AllArgsConstructor
public class UserProgressSummaryResponse {

    private String userId;
    private long totalAttempted;
    private long totalCompleted;
    private long totalFailed;
    private long totalInProgress;
}
