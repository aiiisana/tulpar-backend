package kz.diploma.tulpar.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.util.List;

@Data
public class CreateGrammarRuleRequest {
    @NotBlank private String title;
    @NotBlank private String explanation;
    private List<String> examples;
}
