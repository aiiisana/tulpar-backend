package kz.diploma.tulpar.dto.response;

import lombok.Builder;
import lombok.Data;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class FlashcardResponse {
    private UUID id;
    private String wordRu;
    private String wordKz;
    private String transcription;
    private String exampleSentence;
}
