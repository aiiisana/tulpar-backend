package kz.diploma.tulpar.dto.response;

import lombok.Builder;
import lombok.Data;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class GrammarRuleResponse {
    private UUID id;
    private String title;
    private String explanation;
    private List<String> examples;
    private Instant createdAt;
}
