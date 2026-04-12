package kz.diploma.tulpar.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import kz.diploma.tulpar.dto.request.AddSessionTimeRequest;
import kz.diploma.tulpar.dto.response.SessionTimeResponse;
import kz.diploma.tulpar.security.UserPrincipal;
import kz.diploma.tulpar.service.SessionTimeService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Session Time", description = "Track time spent in the app per day")
@RestController
@RequestMapping("/sessions/time")
@RequiredArgsConstructor
public class SessionTimeController {

    private final SessionTimeService sessionTimeService;

    @Operation(
        summary = "Add session seconds",
        description = "Flush seconds from the current session. Safe to call multiple times (additive upsert)."
    )
    @PostMapping
    public ResponseEntity<SessionTimeResponse> addTime(
            @Parameter(hidden = true) @AuthenticationPrincipal UserPrincipal principal,
            @Valid @RequestBody AddSessionTimeRequest req) {
        return ResponseEntity.ok(
                sessionTimeService.addSeconds(principal.getUid(), req.getSeconds()));
    }

    @Operation(
        summary = "Get today's session time",
        description = "Returns total seconds spent today + daily goal in seconds."
    )
    @GetMapping("/today")
    public ResponseEntity<SessionTimeResponse> getToday(
            @Parameter(hidden = true) @AuthenticationPrincipal UserPrincipal principal) {
        return ResponseEntity.ok(
                sessionTimeService.getTodayTime(principal.getUid()));
    }
}
