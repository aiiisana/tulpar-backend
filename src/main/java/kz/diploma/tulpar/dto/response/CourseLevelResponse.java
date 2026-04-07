package kz.diploma.tulpar.dto.response;

import lombok.Builder;
import lombok.Data;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.UUID;

@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class CourseLevelResponse {
    private UUID id;
    private String title;
    private int orderIndex;
    private List<LessonResponse> lessons;
}
