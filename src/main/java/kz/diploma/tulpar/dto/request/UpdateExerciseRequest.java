package kz.diploma.tulpar.dto.request;

import kz.diploma.tulpar.domain.enums.DifficultyLevel;
import lombok.Data;

import java.util.List;

/**
 * Partial update request — all fields are optional.
 * Only non-null fields will be applied by the service.
 */
@Data
public class UpdateExerciseRequest {

    private DifficultyLevel difficultyLevel;
    private String question;
    private String explanation;

    // vocabulary
    private String word;
    private String translation;

    // listening
    private String audioUrl;
    private String transcript;

    // video
    private String videoUrl;
    private Double startTime;
    private Double endTime;

    // image
    private String imageUrl;

    // shared options
    private List<String> options;
    private String correctAnswer;

    // sentence builder
    private String correctSentence;
    private List<String> shuffledWords;
}
