package kz.diploma.tulpar.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import kz.diploma.tulpar.domain.enums.DifficultyLevel;
import kz.diploma.tulpar.dto.response.ArticleResponse;
import kz.diploma.tulpar.dto.response.PageResponse;
import kz.diploma.tulpar.service.ArticleService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@Tag(name = "Articles", description = "Reading content for language learners")
@RestController
@RequestMapping("/articles")
@RequiredArgsConstructor
public class ArticleController {

    private final ArticleService articleService;

    @Operation(summary = "List articles", description = "Paginated list without full content. Filter by difficulty.")
    @ApiResponse(responseCode = "200", description = "Article page returned")
    @GetMapping
    public ResponseEntity<PageResponse<ArticleResponse>> listArticles(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @Parameter(schema = @Schema(implementation = DifficultyLevel.class))
            @RequestParam(required = false) DifficultyLevel difficulty) {
        return ResponseEntity.ok(articleService.findAll(page, size, difficulty));
    }

    @Operation(summary = "Get article by ID", description = "Returns full article content.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Article returned"),
        @ApiResponse(responseCode = "404", description = "Not found", content = @Content)
    })
    @GetMapping("/{id}")
    public ResponseEntity<ArticleResponse> getArticle(@PathVariable UUID id) {
        return ResponseEntity.ok(articleService.findById(id));
    }
}
