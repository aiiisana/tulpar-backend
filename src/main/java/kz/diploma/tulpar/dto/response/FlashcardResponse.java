package kz.diploma.tulpar.dto.response;

import lombok.Builder;
import lombok.Data;

import java.util.UUID;

@Data @Builder
public class FlashcardResponse {
    private UUID id;
    private String wordRu;
    private String wordKz;
    private String transcription;
    private String exampleSentence;
}
