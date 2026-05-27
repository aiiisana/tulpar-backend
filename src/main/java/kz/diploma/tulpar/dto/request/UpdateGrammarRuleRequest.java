package kz.diploma.tulpar.dto.request;

import lombok.Data;

import java.util.List;

/** All fields are optional — only non-null values will be applied. */
@Data
public class UpdateGrammarRuleRequest {
    private String title;
    private String explanation;
    private List<String> examples;
}
