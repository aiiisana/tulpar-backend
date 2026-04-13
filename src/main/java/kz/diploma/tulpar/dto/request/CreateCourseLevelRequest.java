package kz.diploma.tulpar.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import kz.diploma.tulpar.domain.enums.DifficultyLevel;
import lombok.Data;

import java.util.UUID;

@Data
public class CreateCourseLevelRequest {
    @NotNull
    private UUID courseId;
    @NotBlank
    private String title;
    private int orderIndex;
    /**
     * Required so course-level filtering by user.level works after creation.
     */
    private DifficultyLevel difficultyLevel;
}
