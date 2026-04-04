package kz.diploma.tulpar.dto.response;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Data @Builder
public class DailyChallengeResponse {
    private UUID id;
    private LocalDate challengeDate;
    private List<String> letters;
    private List<String> imageUrls;
    // correctWord intentionally omitted from response
}
