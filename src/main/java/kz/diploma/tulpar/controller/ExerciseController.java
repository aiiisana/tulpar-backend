package kz.diploma.tulpar.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import kz.diploma.tulpar.domain.enums.DifficultyLevel;
import kz.diploma.tulpar.domain.enums.ExerciseType;
import kz.diploma.tulpar.dto.response.ExerciseResponse;
import kz.diploma.tulpar.dto.response.PageResponse;
import kz.diploma.tulpar.service.ExerciseService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

/**
 * Public-facing exercise endpoints for the Flutter mobile client.
 * All routes require an authenticated user (any role).
 */
@Tag(name = "Exercises", description = "Browsing and retrieving exercises (any authenticated user)")
@RestController
@RequestMapping("/exercises")
@RequiredArgsConstructor
public class ExerciseController {

    private final ExerciseService exerciseService;

    @Operation(
        summary = "List exercises",
        description = "Returns a paginated list of exercises. Optionally filter by type and/or difficulty level."
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Page of exercises returned"),
        @ApiResponse(responseCode = "401", description = "Missing or invalid Firebase token", content = @Content)
    })
    @GetMapping
    public ResponseEntity<PageResponse<ExerciseResponse>> listExercises(
            @Parameter(description = "Zero-based page index", example = "0")
            @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size (max 100 recommended)", example = "20")
            @RequestParam(defaultValue = "20") int size,
            @Parameter(description = "Filter by exercise type", schema = @Schema(implementation = ExerciseType.class))
            @RequestParam(required = false) ExerciseType type,
            @Parameter(description = "Filter by difficulty level", schema = @Schema(implementation = DifficultyLevel.class))
            @RequestParam(required = false) DifficultyLevel difficulty) {

        return ResponseEntity.ok(exerciseService.findAll(page, size, type, difficulty));
    }

    @Operation(
        summary = "Get exercise by ID",
        description = "Returns full exercise detail. The correct answer is NOT included in the response."
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Exercise found"),
        @ApiResponse(responseCode = "401", description = "Missing or invalid Firebase token", content = @Content),
        @ApiResponse(responseCode = "404", description = "Exercise not found", content = @Content)
    })
    @GetMapping("/{id}")
    public ResponseEntity<ExerciseResponse> getExercise(
            @Parameter(description = "Exercise UUID", required = true)
            @PathVariable UUID id) {
        return ResponseEntity.ok(exerciseService.findById(id));
    }
}
