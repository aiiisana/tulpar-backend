package kz.diploma.tulpar.dto.response;

import lombok.Builder;
import lombok.Data;

import java.util.UUID;

@Data @Builder
public class CourseResponse {
    private UUID id;
    private String title;
    private String description;
    private int orderIndex;
}
