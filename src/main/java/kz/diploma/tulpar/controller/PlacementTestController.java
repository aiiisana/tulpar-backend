package kz.diploma.tulpar.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import kz.diploma.tulpar.dto.request.PlacementSubmitRequest;
import kz.diploma.tulpar.dto.response.PlacementQuestionResponse;
import kz.diploma.tulpar.dto.response.PlacementResultResponse;
import kz.diploma.tulpar.security.UserPrincipal;
import kz.diploma.tulpar.service.PlacementTestService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "Placement Test", description = "Auto-detect user's Kazakh language level")
@RestController
@RequestMapping("/placement-test")
@RequiredArgsConstructor
public class PlacementTestController {

    private final PlacementTestService placementTestService;

    @Operation(summary = "Get placement test questions",
               description = "Returns all 12 questions ordered by difficulty. " +
                             "Correct answers are intentionally omitted.")
    @GetMapping("/questions")
    public ResponseEntity<List<PlacementQuestionResponse>> getQuestions() {
        return ResponseEntity.ok(placementTestService.getQuestions());
    }

    @Operation(summary = "Submit placement test answers",
               description = "Scores answers, determines the user's level, " +
                             "saves it to their profile and awards the PLACEMENT_DONE achievement.")
    @PostMapping("/submit")
    public ResponseEntity<PlacementResultResponse> submit(
            @AuthenticationPrincipal UserPrincipal principal,
            @Valid @RequestBody PlacementSubmitRequest req) {
        return ResponseEntity.ok(
                placementTestService.submit(principal.getUid(), req));
    }
}
