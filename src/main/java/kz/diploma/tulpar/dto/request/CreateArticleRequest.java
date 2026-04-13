package kz.diploma.tulpar.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import kz.diploma.tulpar.domain.enums.DifficultyLevel;
import lombok.Data;

@Data
public class CreateArticleRequest {
    @NotBlank
    private String title;
    private String titleKz;
    @NotBlank
    private String content;
    private String contentKz;
    @NotNull
    private DifficultyLevel difficultyLevel;
    private String imageUrl;
}
