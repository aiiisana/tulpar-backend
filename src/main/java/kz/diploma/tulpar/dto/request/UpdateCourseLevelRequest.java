package kz.diploma.tulpar.dto.request;

import kz.diploma.tulpar.domain.enums.DifficultyLevel;
import lombok.Data;

/** All fields are optional — only non-null values will be applied. */
@Data
public class UpdateCourseLevelRequest {
    private String title;
    private Integer orderIndex;
    private DifficultyLevel difficultyLevel;
}
