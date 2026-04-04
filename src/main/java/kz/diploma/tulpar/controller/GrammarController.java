package kz.diploma.tulpar.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import kz.diploma.tulpar.dto.response.GrammarRuleResponse;
import kz.diploma.tulpar.service.GrammarService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@Tag(name = "Grammar", description = "Kazakh grammar rules and explanations")
@RestController
@RequestMapping("/grammar")
@RequiredArgsConstructor
public class GrammarController {

    private final GrammarService grammarService;

    @Operation(summary = "List all grammar rules")
    @ApiResponse(responseCode = "200", description = "Grammar rules returned")
    @GetMapping
    public ResponseEntity<List<GrammarRuleResponse>> listGrammarRules() {
        return ResponseEntity.ok(grammarService.findAll());
    }

    @Operation(summary = "Get grammar rule by ID")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Rule returned"),
        @ApiResponse(responseCode = "404", description = "Not found", content = @Content)
    })
    @GetMapping("/{id}")
    public ResponseEntity<GrammarRuleResponse> getRule(@PathVariable UUID id) {
        return ResponseEntity.ok(grammarService.findById(id));
    }
}
