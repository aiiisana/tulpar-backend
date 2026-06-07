package kz.diploma.tulpar.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import kz.diploma.tulpar.dto.request.PracticeMessageRequest;
import kz.diploma.tulpar.dto.response.PracticeHistoryItemResponse;
import kz.diploma.tulpar.dto.response.PracticeResponse;
import kz.diploma.tulpar.security.UserPrincipal;
import kz.diploma.tulpar.service.PracticeService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "Practice", description = "Conversational Kazakh practice with AI corrections")
@RestController
@RequestMapping("/practice")
@RequiredArgsConstructor
public class PracticeController {

    private final PracticeService practiceService;

    /**
     * POST /practice
     * Sends user's Kazakh text to AI; saves exchange to DB; returns reply + corrections.
     */
    @Operation(summary = "Send a practice message")
    @PostMapping
    public ResponseEntity<PracticeResponse> practice(
            @AuthenticationPrincipal UserPrincipal principal,
            @Valid @RequestBody PracticeMessageRequest req) {
        return ResponseEntity.ok(
                practiceService.practice(principal.getUid(), req.getText()));
    }

    /**
     * GET /practice/history
     * Returns the last 100 exchanges for the authenticated user, oldest-first.
     */
    @Operation(summary = "Get conversation history")
    @GetMapping("/history")
    public ResponseEntity<List<PracticeHistoryItemResponse>> history(
            @AuthenticationPrincipal UserPrincipal principal) {
        return ResponseEntity.ok(practiceService.getHistory(principal.getUid()));
    }

    /**
     * DELETE /practice/history
     * Clears all practice messages for the authenticated user.
     */
    @Operation(summary = "Clear conversation history")
    @DeleteMapping("/history")
    public ResponseEntity<Void> clearHistory(
            @AuthenticationPrincipal UserPrincipal principal) {
        practiceService.clearHistory(principal.getUid());
        return ResponseEntity.noContent().build();
    }
}
