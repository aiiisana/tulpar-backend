package kz.diploma.tulpar.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class CreateCourseRequest {
    @NotBlank private String title;
    private String description;
    private int orderIndex;
}
