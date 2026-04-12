package kz.diploma.tulpar.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SessionTimeResponse {

    /** Total seconds spent in the app today. */
    private int totalSeconds;

    /** User's daily goal in seconds (converted from DailyGoal enum). */
    private int goalSeconds;

    /** Whether the user has already met their daily goal today. */
    private boolean goalMet;
}
