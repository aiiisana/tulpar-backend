package kz.diploma.tulpar.dto.request;

import lombok.Data;

/** All fields are optional — only non-null values will be applied. */
@Data
public class UpdateLessonRequest {
    private String title;
    private Integer orderIndex;
    private Integer xpReward;
}
