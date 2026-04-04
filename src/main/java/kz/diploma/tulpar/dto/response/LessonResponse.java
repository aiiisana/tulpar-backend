package kz.diploma.tulpar.dto.response;

import lombok.Builder;
import lombok.Data;

import java.util.List;
import java.util.UUID;

@Data @Builder
public class LessonResponse {
    private UUID id;
    private String title;
    private int orderIndex;
    private int xpReward;
    private boolean unlocked;
    private List<ExerciseResponse> exercises;  // populated only in detail view
}
