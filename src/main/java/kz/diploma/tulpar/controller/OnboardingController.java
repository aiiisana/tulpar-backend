package kz.diploma.tulpar.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import kz.diploma.tulpar.dto.request.SetDailyGoalRequest;
import kz.diploma.tulpar.dto.request.SetLevelRequest;
import kz.diploma.tulpar.security.UserPrincipal;
import kz.diploma.tulpar.service.OnboardingService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Onboarding", description = "User onboarding flow — level selection, daily goal, completion")
@RestController
@RequestMapping("/onboarding")
@RequiredArgsConstructor
public class OnboardingController {

    private final OnboardingService onboardingService;

    @Operation(summary = "Set language level", description = "Step 1: Set the user's self-assessed language level.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Level saved"),
        @ApiResponse(responseCode = "400", description = "Invalid level value")
    })
    @PostMapping("/level")
    public ResponseEntity<Void> setLevel(
            @Parameter(hidden = true) @AuthenticationPrincipal UserPrincipal principal,
            @Valid @RequestBody SetLevelRequest req) {
        onboardingService.setLevel(principal.getUid(), req.getLevel());
        return ResponseEntity.ok().build();
    }

    @Operation(summary = "Set daily goal", description = "Step 2: Set the user's preferred daily study goal.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Goal saved"),
        @ApiResponse(responseCode = "400", description = "Invalid goal value")
    })
    @PostMapping("/goal")
    public ResponseEntity<Void> setGoal(
            @Parameter(hidden = true) @AuthenticationPrincipal UserPrincipal principal,
            @Valid @RequestBody SetDailyGoalRequest req) {
        onboardingService.setDailyGoal(principal.getUid(), req.getDailyGoal());
        return ResponseEntity.ok().build();
    }

    @Operation(summary = "Complete onboarding", description = "Step 3: Mark onboarding as completed.")
    @ApiResponse(responseCode = "200", description = "Onboarding marked complete")
    @PostMapping("/complete")
    public ResponseEntity<Void> complete(
            @Parameter(hidden = true) @AuthenticationPrincipal UserPrincipal principal) {
        onboardingService.complete(principal.getUid());
        return ResponseEntity.ok().build();
    }
}
