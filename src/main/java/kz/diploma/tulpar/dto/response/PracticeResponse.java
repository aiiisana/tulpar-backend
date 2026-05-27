package kz.diploma.tulpar.dto.response;

import lombok.Builder;
import lombok.Data;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Response from the conversational Kazakh practice endpoint.
 * {@code reply} is the AI's natural-language response in Kazakh.
 * {@code corrections} is the list of grammar/vocabulary mistakes found.
 * If the input was correct, {@code corrections} is an empty list.
 */
@Data
@Builder @NoArgsConstructor @AllArgsConstructor
public class PracticeResponse {

    /** The AI's conversational reply in Kazakh. */
    private String reply;

    /** Whether the user's input contained any errors. */
    private boolean hasErrors;

    /** Zero or more corrections for the user's input. */
    private List<Correction> corrections;

    @Data
    @Builder @NoArgsConstructor @AllArgsConstructor
    public static class Correction {
        /** The incorrect fragment as the user wrote it. */
        private String original;
        /** The corrected form. */
        private String corrected;
        /** Short explanation in Russian (matches app default language). */
        private String explanation;
    }
}
