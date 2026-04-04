package kz.diploma.tulpar.dto.response;

import kz.diploma.tulpar.domain.enums.DifficultyLevel;
import lombok.Builder;
import lombok.Data;

import java.time.Instant;
import java.util.UUID;

@Data @Builder
public class ArticleResponse {
    private UUID id;
    private String title;
    private String content;   // null in list responses
    private DifficultyLevel difficultyLevel;
    private Instant createdAt;
}
