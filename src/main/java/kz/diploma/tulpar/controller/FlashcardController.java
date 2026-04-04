package kz.diploma.tulpar.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import kz.diploma.tulpar.dto.response.FlashcardResponse;
import kz.diploma.tulpar.dto.response.PageResponse;
import kz.diploma.tulpar.service.FlashcardService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Flashcards", description = "Vocabulary flashcard browsing")
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
}
