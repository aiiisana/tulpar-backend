package kz.diploma.tulpar.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import kz.diploma.tulpar.dto.response.DailyChallengeResponse;
import kz.diploma.tulpar.service.DailyChallengeService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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
}
