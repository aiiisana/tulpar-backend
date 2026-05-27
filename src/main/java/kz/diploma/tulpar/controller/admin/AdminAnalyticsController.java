package kz.diploma.tulpar.controller.admin;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import kz.diploma.tulpar.dto.response.AnalyticsResponse;
import kz.diploma.tulpar.service.AnalyticsService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "Admin — Analytics",
     description = "Content performance analytics for admins and content managers")
@RestController
@RequestMapping("/admin/analytics")
@PreAuthorize("hasAnyRole('ADMIN','CONTENT_MANAGER')")
@RequiredArgsConstructor
public class AdminAnalyticsController {

    private final AnalyticsService analyticsService;

    @Operation(summary = "Full analytics dashboard",
               description = "Returns DAU (30 days), top-error exercises, and lesson drop-off in one call.")
    @GetMapping
    public ResponseEntity<AnalyticsResponse> getFullAnalytics() {
        return ResponseEntity.ok(analyticsService.getFullAnalytics());
    }

    @Operation(summary = "Daily active users — last N days (default 30)")
    @GetMapping("/dau")
    public ResponseEntity<List<AnalyticsResponse.DauEntry>> getDau(
            @RequestParam(defaultValue = "30") int days) {
        return ResponseEntity.ok(analyticsService.getDailyActiveUsers(days));
    }

    @Operation(summary = "Top exercises by error rate")
    @GetMapping("/exercises/errors")
    public ResponseEntity<List<AnalyticsResponse.ExerciseErrorStat>> getExerciseErrors(
            @RequestParam(defaultValue = "10") int limit) {
        return ResponseEntity.ok(analyticsService.getTopErrorExercises(limit));
    }

    @Operation(summary = "Lesson drop-off rates")
    @GetMapping("/lessons/dropoff")
    public ResponseEntity<List<AnalyticsResponse.LessonDropoff>> getLessonDropoff(
            @RequestParam(defaultValue = "10") int limit) {
        return ResponseEntity.ok(analyticsService.getLessonDropoffs(limit));
    }
}
