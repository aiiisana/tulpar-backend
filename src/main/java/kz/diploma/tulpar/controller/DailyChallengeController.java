package kz.diploma.tulpar.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import kz.diploma.tulpar.dto.request.SubmitDailyChallengeRequest;
import kz.diploma.tulpar.dto.response.DailyChallengeResponse;
import kz.diploma.tulpar.dto.response.DailyChallengeSubmitResponse;
import kz.diploma.tulpar.security.UserPrincipal;
import kz.diploma.tulpar.service.DailyChallengeService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.UUID;

@Tag(name = "Daily Challenge", description = "4 pics 1 word daily challenge")
@RestController
@RequestMapping("/daily-challenge")
@RequiredArgsConstructor
public class DailyChallengeController {

    private final DailyChallengeService dailyChallengeService;

    @Operation(summary = "Get today's challenge", description = "Returns today's 4-pics-1-word challenge. 404 if none created by admin yet.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Challenge returned"),
        @ApiResponse(responseCode = "404", description = "No challenge for today", content = @Content)
    })
    @GetMapping
    public ResponseEntity<DailyChallengeResponse> getToday() {
        return ResponseEntity.ok(dailyChallengeService.getForToday());
    }

    @Operation(
        summary = "Complete daily challenge (legacy)",
        description = "Records XP without answer check. Prefer /submit instead."
    )
    @PostMapping("/{id}/complete")
    public ResponseEntity<Map<String, Integer>> complete(
            @PathVariable UUID id,
            @AuthenticationPrincipal UserPrincipal principal) {
        int xpAwarded = dailyChallengeService.complete(principal.getUid(), id);
        return ResponseEntity.ok(Map.of("xpAwarded", xpAwarded));
    }

    @Operation(
        summary = "Submit daily challenge answer",
        description = "Verifies the answer against the correct word (case-insensitive). " +
                      "On first correct answer of the day: awards +10 XP and records streak activity. " +
                      "Always returns the correct word so the UI can reveal it. " +
                      "Idempotent: calling again after correct answer returns xpAwarded=0."
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Submit result returned"),
        @ApiResponse(responseCode = "400", description = "Validation error", content = @Content),
        @ApiResponse(responseCode = "404", description = "Challenge not found", content = @Content)
    })
    @PostMapping("/submit")
    public ResponseEntity<DailyChallengeSubmitResponse> submit(
            @Valid @RequestBody SubmitDailyChallengeRequest request,
            @AuthenticationPrincipal UserPrincipal principal) {
        DailyChallengeSubmitResponse result = dailyChallengeService.submit(
                principal.getUid(),
                request.getChallengeId(),
                request.getAnswer()
        );
        return ResponseEntity.ok(result);
    }
}
