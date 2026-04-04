package kz.diploma.tulpar.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
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
@Tag(name = "Progress", description = "Submit answers and query learning progress (any authenticated user)")
@RestController
@RequestMapping("/progress")
@RequiredArgsConstructor
public class ProgressController {

    private final ProgressService progressService;

    @Operation(
        summary = "Submit an answer",
        description = "Submits the user's answer for an exercise. The server validates the answer and records the result."
    )
    @ApiResponses({
        @ApiResponse(responseCode = "201", description = "Answer recorded, result returned"),
        @ApiResponse(responseCode = "400", description = "Validation error (missing fields)", content = @Content),
        @ApiResponse(responseCode = "401", description = "Missing or invalid Firebase token", content = @Content),
        @ApiResponse(responseCode = "404", description = "Exercise not found", content = @Content)
    })
    @PostMapping
    public ResponseEntity<ProgressResponse> submit(
            @Parameter(hidden = true) @AuthenticationPrincipal UserPrincipal principal,
            @Valid @RequestBody SubmitProgressRequest req) {

        ProgressResponse response = progressService.submit(principal.getUid(), req);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @Operation(
        summary = "Get my progress history",
        description = "Returns a paginated list of the authenticated user's exercise attempts."
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Progress history returned"),
        @ApiResponse(responseCode = "401", description = "Missing or invalid Firebase token", content = @Content)
    })
    @GetMapping
    public ResponseEntity<PageResponse<ProgressResponse>> getMyProgress(
            @Parameter(hidden = true) @AuthenticationPrincipal UserPrincipal principal,
            @Parameter(description = "Zero-based page index", example = "0")
            @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size", example = "20")
            @RequestParam(defaultValue = "20") int size) {

        return ResponseEntity.ok(progressService.findByUser(principal.getUid(), page, size));
    }

    @Operation(
        summary = "Get my progress summary",
        description = "Returns aggregate stats for the authenticated user: total completed, failed, and in-progress exercises."
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Summary returned"),
        @ApiResponse(responseCode = "401", description = "Missing or invalid Firebase token", content = @Content)
    })
    @GetMapping("/summary")
    public ResponseEntity<UserProgressSummaryResponse> getSummary(
            @Parameter(hidden = true) @AuthenticationPrincipal UserPrincipal principal) {

        return ResponseEntity.ok(progressService.getSummary(principal.getUid()));
    }
}
