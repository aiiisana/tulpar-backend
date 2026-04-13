package kz.diploma.tulpar.dto.response;

import lombok.Builder;
import lombok.Data;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class DailyChallengeResponse {
    private UUID id;
    private LocalDate challengeDate;
    private List<String> letters;
    private List<String> imageUrls;
    private int wordLength;
    private String correctWord; // sent to client for local answer check
    /** true if the authenticated user has already completed this challenge today */
    private boolean completedByCurrentUser;
}
