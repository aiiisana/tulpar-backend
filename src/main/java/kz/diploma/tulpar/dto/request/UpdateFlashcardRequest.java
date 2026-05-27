package kz.diploma.tulpar.dto.request;

import lombok.Data;

/** All fields are optional — only non-null values will be applied. */
@Data
public class UpdateFlashcardRequest {
    private String wordRu;
    private String wordKz;
    private String transcription;
    private String exampleSentence;
    private String audioUrl;
}
