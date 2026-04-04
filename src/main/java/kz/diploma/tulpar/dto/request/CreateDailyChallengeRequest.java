package kz.diploma.tulpar.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDate;
import java.util.List;

@Data
public class CreateDailyChallengeRequest {
    @NotNull  private LocalDate challengeDate;
    @NotBlank private String correctWord;
    @NotNull  private List<String> letters;
    @NotNull  private List<String> imageUrls;
}
