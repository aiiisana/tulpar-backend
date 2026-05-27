package kz.diploma.tulpar.dto.request;

import lombok.Data;

/** All fields are optional — only non-null values will be applied. */
@Data
public class UpdateCourseRequest {
    private String title;
    private String description;
    private Integer orderIndex;
}
