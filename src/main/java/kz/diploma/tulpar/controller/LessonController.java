package kz.diploma.tulpar.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import kz.diploma.tulpar.dto.response.CourseLevelResponse;
import kz.diploma.tulpar.dto.response.CourseResponse;
import kz.diploma.tulpar.dto.response.LessonResponse;
import kz.diploma.tulpar.security.UserPrincipal;
import kz.diploma.tulpar.service.LessonService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@Tag(name = "Learning Path", description = "Courses, levels and lessons (Duolingo-style map)")
@RestController
@RequiredArgsConstructor
public class LessonController {

    private final LessonService lessonService;

    @Operation(summary = "List all courses")
    @ApiResponse(responseCode = "200", description = "Course list returned")
    @GetMapping("/courses")
    public ResponseEntity<List<CourseResponse>> getCourses() {
        return ResponseEntity.ok(lessonService.getCourses());
    }

    @Operation(summary = "Get course levels with lessons",
               description = "Returns levels of a course, each containing its lessons with unlock status.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Levels returned"),
        @ApiResponse(responseCode = "404", description = "Course not found", content = @Content)
    })
    @GetMapping("/courses/{courseId}/levels")
    public ResponseEntity<List<CourseLevelResponse>> getLevels(
            @PathVariable UUID courseId,
            @Parameter(hidden = true) @AuthenticationPrincipal UserPrincipal principal) {
        return ResponseEntity.ok(lessonService.getCourseLevels(courseId, principal.getUid()));
    }

    @Operation(summary = "Get lesson detail",
               description = "Returns lesson with exercises. Unlock status is included.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Lesson returned"),
        @ApiResponse(responseCode = "404", description = "Lesson not found", content = @Content)
    })
    @GetMapping("/lessons/{lessonId}")
    public ResponseEntity<LessonResponse> getLesson(
            @PathVariable UUID lessonId,
            @Parameter(hidden = true) @AuthenticationPrincipal UserPrincipal principal) {
        return ResponseEntity.ok(lessonService.getLessonDetail(lessonId, principal.getUid()));
    }
}
