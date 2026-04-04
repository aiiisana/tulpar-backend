package kz.diploma.tulpar.dto.response;

import lombok.Builder;
import lombok.Data;

import java.util.List;
import java.util.UUID;

@Data @Builder
public class CourseLevelResponse {
    private UUID id;
    private String title;
    private int orderIndex;
    private List<LessonResponse> lessons;
}
