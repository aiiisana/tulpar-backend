package kz.diploma.tulpar.dto.request;

import jakarta.validation.constraints.NotNull;
import kz.diploma.tulpar.domain.enums.DailyGoal;
import lombok.Data;

@Data
public class SetDailyGoalRequest {
    @NotNull
    private DailyGoal dailyGoal;
}
