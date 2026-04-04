package kz.diploma.tulpar.controller.admin;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import kz.diploma.tulpar.dto.request.CreateGrammarRuleRequest;
import kz.diploma.tulpar.dto.response.GrammarRuleResponse;
import kz.diploma.tulpar.service.GrammarService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@Tag(name = "Admin — Grammar", description = "CRUD operations for grammar rules (ADMIN only)")
@RestController
@RequestMapping("/admin/grammar")
@RequiredArgsConstructor
public class AdminGrammarController {

    private final GrammarService grammarService;

    @Operation(summary = "Create grammar rule", description = "Creates a new grammar rule with examples.")
    @ApiResponses({
        @ApiResponse(responseCode = "201", description = "Grammar rule created"),
        @ApiResponse(responseCode = "400", description = "Validation error", content = @Content),
        @ApiResponse(responseCode = "401", description = "Missing or invalid Firebase token", content = @Content),
        @ApiResponse(responseCode = "403", description = "Insufficient role", content = @Content)
    })
    @PostMapping
    public ResponseEntity<GrammarRuleResponse> createRule(@Valid @RequestBody CreateGrammarRuleRequest req) {
        return ResponseEntity.status(HttpStatus.CREATED).body(grammarService.create(req));
    }

    @Operation(summary = "Delete grammar rule", description = "Permanently deletes a grammar rule.")
    @ApiResponses({
        @ApiResponse(responseCode = "204", description = "Grammar rule deleted"),
        @ApiResponse(responseCode = "401", description = "Missing or invalid Firebase token", content = @Content),
        @ApiResponse(responseCode = "403", description = "Insufficient role", content = @Content),
        @ApiResponse(responseCode = "404", description = "Grammar rule not found", content = @Content)
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteRule(
            @Parameter(description = "Grammar rule UUID", required = true) @PathVariable UUID id) {
        grammarService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
