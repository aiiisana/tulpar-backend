package kz.diploma.tulpar.dto.request;

import kz.diploma.tulpar.domain.enums.DailyGoal;
import lombok.Data;

@Data
public class UpdateProfileRequest {
    private String username;
    private String avatarUrl;
    private Boolean notificationsEnabled;
    private DailyGoal dailyGoal;
}
