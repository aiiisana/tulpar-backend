package kz.diploma.tulpar.dto.request;

import kz.diploma.tulpar.domain.enums.DifficultyLevel;
import lombok.Data;

/** All fields are optional — only non-null values will be applied. */
@Data
public class UpdateArticleRequest {
    private String title;
    private String titleKz;
    private String content;
    private String contentKz;
    private DifficultyLevel difficultyLevel;
    private String imageUrl;
}
