package kz.diploma.tulpar.controller.admin;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import kz.diploma.tulpar.dto.request.CreateArticleRequest;
import kz.diploma.tulpar.dto.response.ArticleResponse;
import kz.diploma.tulpar.service.ArticleService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@Tag(name = "Admin — Articles", description = "CRUD operations for reading articles (ADMIN only)")
@RestController
@RequestMapping("/admin/articles")
@RequiredArgsConstructor
public class AdminArticleController {

    private final ArticleService articleService;

    @Operation(summary = "Create article", description = "Creates a new reading article.")
    @ApiResponses({
        @ApiResponse(responseCode = "201", description = "Article created"),
        @ApiResponse(responseCode = "400", description = "Validation error", content = @Content),
        @ApiResponse(responseCode = "401", description = "Missing or invalid Firebase token", content = @Content),
        @ApiResponse(responseCode = "403", description = "Insufficient role", content = @Content)
    })
    @PostMapping
    public ResponseEntity<ArticleResponse> createArticle(@Valid @RequestBody CreateArticleRequest req) {
        return ResponseEntity.status(HttpStatus.CREATED).body(articleService.create(req));
    }

    @Operation(summary = "Delete article", description = "Permanently deletes an article.")
    @ApiResponses({
        @ApiResponse(responseCode = "204", description = "Article deleted"),
        @ApiResponse(responseCode = "401", description = "Missing or invalid Firebase token", content = @Content),
        @ApiResponse(responseCode = "403", description = "Insufficient role", content = @Content),
        @ApiResponse(responseCode = "404", description = "Article not found", content = @Content)
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteArticle(
            @Parameter(description = "Article UUID", required = true) @PathVariable UUID id) {
        articleService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
