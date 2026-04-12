package kz.diploma.tulpar.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class CreateFlashcardRequest {
    @NotBlank
    private String wordRu;
    @NotBlank
    private String wordKz;
    private String transcription;
    private String exampleSentence;
    private String audioUrl;
}
