package kz.diploma.tulpar.controller.admin;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import kz.diploma.tulpar.dto.request.CreateExerciseRequest;
import kz.diploma.tulpar.dto.request.UpdateExerciseRequest;
import kz.diploma.tulpar.dto.response.ExerciseResponse;
import kz.diploma.tulpar.service.ExerciseService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

/**
 * Admin/Content-Manager CRUD endpoints for exercises.
 * Security: roles ADMIN or CONTENT_MANAGER (enforced in SecurityConfig).
 */
@Tag(name = "Admin — Exercises", description = "CRUD operations for exercises (ADMIN or CONTENT_MANAGER)")
@RestController
@RequestMapping("/admin/exercises")
@RequiredArgsConstructor
public class AdminExerciseController {

    private final ExerciseService exerciseService;

    @Operation(
        summary = "Create exercise",
        description = "Creates a new exercise of any type. Fields irrelevant to the chosen type can be omitted."
    )
    @ApiResponses({
        @ApiResponse(responseCode = "201", description = "Exercise created"),
        @ApiResponse(responseCode = "400", description = "Validation error", content = @Content),
        @ApiResponse(responseCode = "401", description = "Missing or invalid Firebase token", content = @Content),
        @ApiResponse(responseCode = "403", description = "Insufficient role", content = @Content)
    })
    @PostMapping
    public ResponseEntity<ExerciseResponse> createExercise(
            @Valid @RequestBody CreateExerciseRequest req) {
        return ResponseEntity.status(HttpStatus.CREATED).body(exerciseService.create(req));
    }

    @Operation(
        summary = "Update exercise",
        description = "Partially updates an exercise. Only non-null fields are applied."
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Exercise updated"),
        @ApiResponse(responseCode = "401", description = "Missing or invalid Firebase token", content = @Content),
        @ApiResponse(responseCode = "403", description = "Insufficient role", content = @Content),
        @ApiResponse(responseCode = "404", description = "Exercise not found", content = @Content)
    })
    @PutMapping("/{id}")
    public ResponseEntity<ExerciseResponse> updateExercise(
            @Parameter(description = "Exercise UUID", required = true)
            @PathVariable UUID id,
            @RequestBody UpdateExerciseRequest req) {
        return ResponseEntity.ok(exerciseService.update(id, req));
    }

    @Operation(
        summary = "Delete exercise",
        description = "Permanently deletes an exercise and all associated progress records."
    )
    @ApiResponses({
        @ApiResponse(responseCode = "204", description = "Exercise deleted"),
        @ApiResponse(responseCode = "401", description = "Missing or invalid Firebase token", content = @Content),
        @ApiResponse(responseCode = "403", description = "Insufficient role", content = @Content),
        @ApiResponse(responseCode = "404", description = "Exercise not found", content = @Content)
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteExercise(
            @Parameter(description = "Exercise UUID", required = true)
            @PathVariable UUID id) {
        exerciseService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
