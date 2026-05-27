package kz.diploma.tulpar.dto.response;

import kz.diploma.tulpar.domain.enums.DifficultyLevel;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class PlacementResultResponse {
    private int totalQuestions;
    private int correctAnswers;
    private int scorePercent;
    private DifficultyLevel determinedLevel;
    private String levelLabel;   // human-readable Russian label
    private String message;
}
