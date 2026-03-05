package kz.diploma.tulpar.controller;

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
@RestController
@RequestMapping("/exercises")
@RequiredArgsConstructor
public class ExerciseController {

    private final ExerciseService exerciseService;

    /**
     * GET /exercises?page=0&size=20&type=VOCABULARY&difficulty=BEGINNER
     * Returns a paginated list of exercises, optionally filtered.
     */
    @GetMapping
    public ResponseEntity<PageResponse<ExerciseResponse>> listExercises(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) ExerciseType type,
            @RequestParam(required = false) DifficultyLevel difficulty) {

        return ResponseEntity.ok(exerciseService.findAll(page, size, type, difficulty));
    }

    /**
     * GET /exercises/{id}
     * Returns full exercise detail. The correct answer is NOT included in the response.
     */
    @GetMapping("/{id}")
    public ResponseEntity<ExerciseResponse> getExercise(@PathVariable UUID id) {
        return ResponseEntity.ok(exerciseService.findById(id));
    }
}
