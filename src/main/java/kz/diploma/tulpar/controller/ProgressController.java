package kz.diploma.tulpar.controller;

import jakarta.validation.Valid;
import kz.diploma.tulpar.dto.request.SubmitProgressRequest;
import kz.diploma.tulpar.dto.response.PageResponse;
import kz.diploma.tulpar.dto.response.ProgressResponse;
import kz.diploma.tulpar.dto.response.UserProgressSummaryResponse;
import kz.diploma.tulpar.security.UserPrincipal;
import kz.diploma.tulpar.service.ProgressService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

/**
 * Endpoints for submitting and querying user progress.
 * Users can only access their own progress data.
 */
@RestController
@RequestMapping("/progress")
@RequiredArgsConstructor
public class ProgressController {

    private final ProgressService progressService;

    /**
     * POST /progress
     * Submit an answer for an exercise. Server validates the answer.
     */
    @PostMapping
    public ResponseEntity<ProgressResponse> submit(
            @AuthenticationPrincipal UserPrincipal principal,
            @Valid @RequestBody SubmitProgressRequest req) {

        ProgressResponse response = progressService.submit(principal.getUid(), req);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * GET /progress?page=0&size=20
     * Returns the authenticated user's progress history.
     */
    @GetMapping
    public ResponseEntity<PageResponse<ProgressResponse>> getMyProgress(
            @AuthenticationPrincipal UserPrincipal principal,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {

        return ResponseEntity.ok(progressService.findByUser(principal.getUid(), page, size));
    }

    /**
     * GET /progress/summary
     * Returns aggregate stats (total completed, failed, in-progress).
     */
    @GetMapping("/summary")
    public ResponseEntity<UserProgressSummaryResponse> getSummary(
            @AuthenticationPrincipal UserPrincipal principal) {

        return ResponseEntity.ok(progressService.getSummary(principal.getUid()));
    }
}
