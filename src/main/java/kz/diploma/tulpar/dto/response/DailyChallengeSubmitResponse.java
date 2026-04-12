package kz.diploma.tulpar.dto.response;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class DailyChallengeSubmitResponse {

    /** True if the provided answer matched the correct word. */
    private boolean correct;

    /** XP awarded this submission (10 if first correct answer today, else 0). */
    private int xpAwarded;

    /** The correct word — revealed after submission. */
    private String correctWord;
}
