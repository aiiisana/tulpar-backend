package kz.diploma.tulpar.dto.request;

import jakarta.validation.constraints.NotNull;
import kz.diploma.tulpar.domain.enums.DifficultyLevel;
import lombok.Data;

@Data
public class SetLevelRequest {
    @NotNull
    private DifficultyLevel level;
}
