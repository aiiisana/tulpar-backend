package kz.diploma.tulpar.controller.admin;

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
@RestController
@RequestMapping("/admin/exercises")
@RequiredArgsConstructor
public class AdminExerciseController {

    private final ExerciseService exerciseService;

    /**
     * POST /admin/exercises
     * Create a new exercise of any type.
     */
    @PostMapping
    public ResponseEntity<ExerciseResponse> createExercise(
            @Valid @RequestBody CreateExerciseRequest req) {
        return ResponseEntity.status(HttpStatus.CREATED).body(exerciseService.create(req));
    }

    /**
     * PUT /admin/exercises/{id}
     * Partially update an exercise. Only non-null fields are applied.
     */
    @PutMapping("/{id}")
    public ResponseEntity<ExerciseResponse> updateExercise(
            @PathVariable UUID id,
            @RequestBody UpdateExerciseRequest req) {
        return ResponseEntity.ok(exerciseService.update(id, req));
    }

    /**
     * DELETE /admin/exercises/{id}
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteExercise(@PathVariable UUID id) {
        exerciseService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
