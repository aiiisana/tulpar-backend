package kz.diploma.tulpar.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import kz.diploma.tulpar.dto.request.SrsAnswerRequest;
import kz.diploma.tulpar.dto.response.SrsCardResponse;
import kz.diploma.tulpar.security.UserPrincipal;
import kz.diploma.tulpar.service.SpacedRepetitionService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@Tag(name = "Spaced Repetition", description = "SM-2 flashcard review queue")
@RestController
@RequestMapping("/review")
@RequiredArgsConstructor
public class SpacedRepetitionController {

    private final SpacedRepetitionService srsService;

    /** Returns up to 50 exercises that are due for review. */
    @Operation(summary = "Get due cards")
    @GetMapping("/due")
    public ResponseEntity<List<SrsCardResponse>> getDue(
            @AuthenticationPrincipal UserPrincipal principal) {
        return ResponseEntity.ok(srsService.getDueCards(principal.getUid()));
    }

    /** Count of due cards (for badge display on home screen). */
    @Operation(summary = "Count due cards")
    @GetMapping("/due/count")
    public ResponseEntity<Map<String, Long>> countDue(
            @AuthenticationPrincipal UserPrincipal principal) {
        return ResponseEntity.ok(
                Map.of("count", srsService.countDue(principal.getUid())));
    }

    /** Record an answer during a review session — advances the SM-2 state. */
    @Operation(summary = "Submit review answer")
    @PostMapping("/answer")
    public ResponseEntity<Void> answer(
            @AuthenticationPrincipal UserPrincipal principal,
            @Valid @RequestBody SrsAnswerRequest req) {
        srsService.recordAnswer(principal.getUid(), req);
        return ResponseEntity.noContent().build();
    }
}
