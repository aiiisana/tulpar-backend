package kz.diploma.tulpar.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import kz.diploma.tulpar.dto.response.CalendarDayResponse;
import kz.diploma.tulpar.dto.response.StatsResponse;
import kz.diploma.tulpar.security.UserPrincipal;
import kz.diploma.tulpar.service.StreakService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Tag(name = "Stats & Streak", description = "Learning streak, XP and weekly calendar")
@RestController
@RequestMapping("/stats")
@RequiredArgsConstructor
public class StatsController {

    private final StreakService streakService;

    @Operation(summary = "Get streak and XP stats", description = "Returns current streak, longest streak, and total XP.")
    @ApiResponse(responseCode = "200", description = "Stats returned")
    @GetMapping
    public ResponseEntity<StatsResponse> getStats(
            @Parameter(hidden = true) @AuthenticationPrincipal UserPrincipal principal) {
        return ResponseEntity.ok(streakService.getStats(principal.getUid()));
    }

    @Operation(summary = "Get weekly calendar", description = "Returns last 7 days with activity completion status.")
    @ApiResponse(responseCode = "200", description = "Calendar returned")
    @GetMapping("/calendar")
    public ResponseEntity<List<CalendarDayResponse>> getCalendar(
            @Parameter(hidden = true) @AuthenticationPrincipal UserPrincipal principal) {
        return ResponseEntity.ok(streakService.getWeeklyCalendar(principal.getUid()));
    }
}
