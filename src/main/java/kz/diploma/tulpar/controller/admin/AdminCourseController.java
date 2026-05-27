package kz.diploma.tulpar.controller.admin;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import kz.diploma.tulpar.dto.request.CreateCourseLevelRequest;
import kz.diploma.tulpar.dto.request.CreateCourseRequest;
import kz.diploma.tulpar.dto.request.CreateDailyChallengeRequest;
import kz.diploma.tulpar.dto.request.CreateLessonRequest;
import kz.diploma.tulpar.dto.request.UpdateCourseRequest;
import kz.diploma.tulpar.dto.request.UpdateCourseLevelRequest;
import kz.diploma.tulpar.dto.request.UpdateLessonRequest;
import kz.diploma.tulpar.dto.response.*;
import kz.diploma.tulpar.service.DailyChallengeService;
import kz.diploma.tulpar.service.LessonService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@Tag(name = "Admin — Courses", description = "CRUD for courses, levels, lessons, and daily challenges (ADMIN only)")
@RestController
@RequestMapping("/admin")
@RequiredArgsConstructor
public class AdminCourseController {

    private final LessonService lessonService;
    private final DailyChallengeService dailyChallengeService;

    // ── Courses ───────────────────────────────────────────────────────────────

    @Operation(summary = "Create course")
    @ApiResponses({
        @ApiResponse(responseCode = "201", description = "Course created"),
        @ApiResponse(responseCode = "400", description = "Validation error", content = @Content),
        @ApiResponse(responseCode = "403", description = "Insufficient role", content = @Content)
    })
    @PostMapping("/courses")
    public ResponseEntity<CourseResponse> createCourse(@Valid @RequestBody CreateCourseRequest req) {
        return ResponseEntity.status(HttpStatus.CREATED).body(lessonService.createCourse(req));
    }

    @Operation(summary = "Update course")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Course updated"),
        @ApiResponse(responseCode = "404", description = "Course not found", content = @Content),
        @ApiResponse(responseCode = "403", description = "Insufficient role", content = @Content)
    })
    @PutMapping("/courses/{id}")
    public ResponseEntity<CourseResponse> updateCourse(
            @Parameter(description = "Course UUID", required = true) @PathVariable UUID id,
            @RequestBody UpdateCourseRequest req) {
        return ResponseEntity.ok(lessonService.updateCourse(id, req));
    }

    @Operation(summary = "Delete course", description = "Permanently deletes a course and its child levels and lessons.")
    @ApiResponses({
        @ApiResponse(responseCode = "204", description = "Course deleted"),
        @ApiResponse(responseCode = "403", description = "Insufficient role", content = @Content),
        @ApiResponse(responseCode = "404", description = "Course not found", content = @Content)
    })
    @DeleteMapping("/courses/{id}")
    public ResponseEntity<Void> deleteCourse(
            @Parameter(description = "Course UUID", required = true) @PathVariable UUID id) {
        lessonService.deleteCourse(id);
        return ResponseEntity.noContent().build();
    }

    // ── Course Levels ─────────────────────────────────────────────────────────

    @Operation(summary = "Create course level")
    @ApiResponses({
        @ApiResponse(responseCode = "201", description = "Level created"),
        @ApiResponse(responseCode = "400", description = "Validation error", content = @Content),
        @ApiResponse(responseCode = "403", description = "Insufficient role", content = @Content),
        @ApiResponse(responseCode = "404", description = "Course not found", content = @Content)
    })
    @PostMapping("/course-levels")
    public ResponseEntity<CourseLevelResponse> createLevel(@Valid @RequestBody CreateCourseLevelRequest req) {
        return ResponseEntity.status(HttpStatus.CREATED).body(lessonService.createLevel(req));
    }

    @Operation(summary = "Update course level")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Level updated"),
        @ApiResponse(responseCode = "404", description = "Level not found", content = @Content),
        @ApiResponse(responseCode = "403", description = "Insufficient role", content = @Content)
    })
    @PutMapping("/course-levels/{id}")
    public ResponseEntity<CourseLevelResponse> updateLevel(
            @Parameter(description = "Level UUID", required = true) @PathVariable UUID id,
            @RequestBody UpdateCourseLevelRequest req) {
        return ResponseEntity.ok(lessonService.updateLevel(id, req));
    }

    @Operation(summary = "Delete course level")
    @ApiResponses({
        @ApiResponse(responseCode = "204", description = "Level deleted"),
        @ApiResponse(responseCode = "403", description = "Insufficient role", content = @Content),
        @ApiResponse(responseCode = "404", description = "Level not found", content = @Content)
    })
    @DeleteMapping("/course-levels/{id}")
    public ResponseEntity<Void> deleteLevel(
            @Parameter(description = "Level UUID", required = true) @PathVariable UUID id) {
        lessonService.deleteLevel(id);
        return ResponseEntity.noContent().build();
    }

    // ── Lessons ───────────────────────────────────────────────────────────────

    @Operation(summary = "Create lesson")
    @ApiResponses({
        @ApiResponse(responseCode = "201", description = "Lesson created"),
        @ApiResponse(responseCode = "400", description = "Validation error", content = @Content),
        @ApiResponse(responseCode = "403", description = "Insufficient role", content = @Content),
        @ApiResponse(responseCode = "404", description = "Course level not found", content = @Content)
    })
    @PostMapping("/lessons")
    public ResponseEntity<LessonResponse> createLesson(@Valid @RequestBody CreateLessonRequest req) {
        return ResponseEntity.status(HttpStatus.CREATED).body(lessonService.createLesson(req));
    }

    @Operation(summary = "Update lesson")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Lesson updated"),
        @ApiResponse(responseCode = "404", description = "Lesson not found", content = @Content),
        @ApiResponse(responseCode = "403", description = "Insufficient role", content = @Content)
    })
    @PutMapping("/lessons/{id}")
    public ResponseEntity<LessonResponse> updateLesson(
            @Parameter(description = "Lesson UUID", required = true) @PathVariable UUID id,
            @RequestBody UpdateLessonRequest req) {
        return ResponseEntity.ok(lessonService.updateLesson(id, req));
    }

    @Operation(summary = "Delete lesson")
    @ApiResponses({
        @ApiResponse(responseCode = "204", description = "Lesson deleted"),
        @ApiResponse(responseCode = "403", description = "Insufficient role", content = @Content),
        @ApiResponse(responseCode = "404", description = "Lesson not found", content = @Content)
    })
    @DeleteMapping("/lessons/{id}")
    public ResponseEntity<Void> deleteLesson(
            @Parameter(description = "Lesson UUID", required = true) @PathVariable UUID id) {
        lessonService.deleteLesson(id);
        return ResponseEntity.noContent().build();
    }

    // ── Daily Challenges ──────────────────────────────────────────────────────

    @Operation(summary = "Create daily challenge", description = "Creates a challenge for a specific date. One challenge per date.")
    @ApiResponses({
        @ApiResponse(responseCode = "201", description = "Challenge created"),
        @ApiResponse(responseCode = "400", description = "Validation error or duplicate date", content = @Content),
        @ApiResponse(responseCode = "403", description = "Insufficient role", content = @Content)
    })
    @PostMapping("/daily-challenges")
    public ResponseEntity<DailyChallengeResponse> createChallenge(@Valid @RequestBody CreateDailyChallengeRequest req) {
        return ResponseEntity.status(HttpStatus.CREATED).body(dailyChallengeService.create(req));
    }
}
