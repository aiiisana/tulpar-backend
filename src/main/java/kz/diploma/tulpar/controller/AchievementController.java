package kz.diploma.tulpar.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import kz.diploma.tulpar.dto.response.AchievementResponse;
import kz.diploma.tulpar.security.UserPrincipal;
import kz.diploma.tulpar.service.AchievementService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Tag(name = "Achievements", description = "User achievement badges and milestones")
@RestController
@RequestMapping("/achievements")
@RequiredArgsConstructor
public class AchievementController {

    private final AchievementService achievementService;

    @Operation(summary = "Get all achievements with earned status",
               description = "Returns the full catalogue. Earned achievements include earnedAt timestamp.")
    @GetMapping
    public ResponseEntity<List<AchievementResponse>> getAchievements(
            @AuthenticationPrincipal UserPrincipal principal) {
        return ResponseEntity.ok(
                achievementService.getForUser(principal.getUid()));
    }
}
