package kz.diploma.tulpar.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class CourseResponse {
    private UUID id;
    private String title;
    private String description;
    private int orderIndex;
}
