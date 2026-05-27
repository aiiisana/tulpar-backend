package kz.diploma.tulpar.dto.response;

import lombok.Builder;
import lombok.Data;

import java.time.Instant;
import java.util.UUID;

@Data
@Builder
public class AchievementResponse {
    private UUID id;
    private String code;
    private String title;
    private String description;
    private String iconName;
    private int xpReward;
    private boolean earned;
    private Instant earnedAt;   // null if not earned
}
