package kz.diploma.tulpar.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;
import java.util.UUID;

@Data
public class PlacementSubmitRequest {
    /** List of {questionId, selectedIndex} pairs for all answered questions. */
    @NotNull
    private List<Answer> answers;

    @Data
    public static class Answer {
        @NotNull
        private UUID questionId;
        private int selectedIndex;
    }
}
