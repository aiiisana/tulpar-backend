package kz.diploma.tulpar.controller.admin;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import kz.diploma.tulpar.dto.request.CreateFlashcardRequest;
import kz.diploma.tulpar.dto.request.UpdateFlashcardRequest;
import kz.diploma.tulpar.dto.response.FlashcardResponse;
import kz.diploma.tulpar.service.FlashcardService;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.CacheManager;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@Tag(name = "Admin — Flashcards", description = "CRUD operations for vocabulary flashcards (ADMIN only)")
@RestController
@RequestMapping("/admin/flashcards")
@RequiredArgsConstructor
public class AdminFlashcardController {

    private final FlashcardService flashcardService;
    private final CacheManager cacheManager;

    @Operation(summary = "Create flashcard", description = "Adds a new vocabulary flashcard.")
    @ApiResponses({
        @ApiResponse(responseCode = "201", description = "Flashcard created"),
        @ApiResponse(responseCode = "400", description = "Validation error", content = @Content),
        @ApiResponse(responseCode = "401", description = "Missing or invalid Firebase token", content = @Content),
        @ApiResponse(responseCode = "403", description = "Insufficient role", content = @Content)
    })
    @PostMapping
    public ResponseEntity<FlashcardResponse> createFlashcard(@Valid @RequestBody CreateFlashcardRequest req) {
        return ResponseEntity.status(HttpStatus.CREATED).body(flashcardService.create(req));
    }

    @Operation(summary = "Update flashcard", description = "Partially updates a flashcard.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Flashcard updated"),
        @ApiResponse(responseCode = "404", description = "Flashcard not found", content = @Content),
        @ApiResponse(responseCode = "403", description = "Insufficient role", content = @Content)
    })
    @PutMapping("/{id}")
    public ResponseEntity<FlashcardResponse> updateFlashcard(
            @Parameter(description = "Flashcard UUID", required = true) @PathVariable UUID id,
            @RequestBody UpdateFlashcardRequest req) {
        return ResponseEntity.ok(flashcardService.update(id, req));
    }

    @Operation(summary = "Delete flashcard", description = "Permanently deletes a flashcard.")
    @ApiResponses({
        @ApiResponse(responseCode = "204", description = "Flashcard deleted"),
        @ApiResponse(responseCode = "401", description = "Missing or invalid Firebase token", content = @Content),
        @ApiResponse(responseCode = "403", description = "Insufficient role", content = @Content),
        @ApiResponse(responseCode = "404", description = "Flashcard not found", content = @Content)
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteFlashcard(
            @Parameter(description = "Flashcard UUID", required = true) @PathVariable UUID id) {
        flashcardService.delete(id);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Evict flashcard cache", description = "Clears the Redis cache for flashcards. Use after bulk data changes.")
    @ApiResponse(responseCode = "204", description = "Cache cleared")
    @PostMapping("/cache/evict")
    public ResponseEntity<Void> evictCache() {
        var cache = cacheManager.getCache("flashcards");
        if (cache != null) cache.clear();
        return ResponseEntity.noContent().build();
    }
}
