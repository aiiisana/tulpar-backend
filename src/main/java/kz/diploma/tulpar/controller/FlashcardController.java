package kz.diploma.tulpar.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import kz.diploma.tulpar.dto.response.FlashcardResponse;
import kz.diploma.tulpar.dto.response.PageResponse;
import kz.diploma.tulpar.security.UserPrincipal;
import kz.diploma.tulpar.service.FlashcardService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@Tag(name = "Flashcards", description = "Vocabulary flashcard browsing and saving")
@RestController
@RequestMapping("/flashcards")
@RequiredArgsConstructor
public class FlashcardController {

    private final FlashcardService flashcardService;

    @Operation(summary = "List flashcards", description = "Returns a paginated list of vocabulary flashcards.")
    @ApiResponse(responseCode = "200", description = "Flashcard page returned")
    @GetMapping
    public ResponseEntity<PageResponse<FlashcardResponse>> listFlashcards(
            @Parameter(description = "Page index", example = "0") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size", example = "20") @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(flashcardService.findAll(page, size));
    }

    @Operation(summary = "Get saved flashcards", description = "Returns all flashcards saved by the current user.")
    @ApiResponse(responseCode = "200", description = "Saved flashcard list returned")
    @GetMapping("/saved")
    public ResponseEntity<List<FlashcardResponse>> getSaved(
            @Parameter(hidden = true) @AuthenticationPrincipal UserPrincipal principal) {
        return ResponseEntity.ok(flashcardService.getSaved(principal.getUid()));
    }

    @Operation(summary = "Check if flashcard is saved")
    @ApiResponse(responseCode = "200", description = "Saved status returned")
    @GetMapping("/{id}/saved")
    public ResponseEntity<Map<String, Boolean>> isSaved(
            @PathVariable UUID id,
            @Parameter(hidden = true) @AuthenticationPrincipal UserPrincipal principal) {
        return ResponseEntity.ok(Map.of("saved", flashcardService.isSaved(principal.getUid(), id)));
    }

    @Operation(summary = "Save a flashcard")
    @ApiResponse(responseCode = "204", description = "Flashcard saved")
    @PostMapping("/{id}/save")
    public ResponseEntity<Void> save(
            @PathVariable UUID id,
            @Parameter(hidden = true) @AuthenticationPrincipal UserPrincipal principal) {
        flashcardService.save(principal.getUid(), id);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Remove a saved flashcard")
    @ApiResponse(responseCode = "204", description = "Flashcard removed from saved")
    @DeleteMapping("/{id}/save")
    public ResponseEntity<Void> unsave(
            @PathVariable UUID id,
            @Parameter(hidden = true) @AuthenticationPrincipal UserPrincipal principal) {
        flashcardService.unsave(principal.getUid(), id);
        return ResponseEntity.noContent().build();
    }
}
