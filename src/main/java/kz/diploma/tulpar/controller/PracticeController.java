package kz.diploma.tulpar.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import kz.diploma.tulpar.dto.request.PracticeMessageRequest;
import kz.diploma.tulpar.dto.response.PracticeResponse;
import kz.diploma.tulpar.service.PracticeService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import kz.diploma.tulpar.security.UserPrincipal;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Practice", description = "Conversational Kazakh practice with AI corrections")
@RestController
@RequestMapping("/practice")
@RequiredArgsConstructor
public class PracticeController {

    private final PracticeService practiceService;

    /**
     * POST /practice
     * User sends a message in Kazakh; AI replies and returns corrections.
     */
    @Operation(summary = "Converse in Kazakh",
            description = "Sends user Kazakh text to AI; returns a reply and a list of corrections.")
    @PostMapping
    public ResponseEntity<PracticeResponse> practice(
            @AuthenticationPrincipal UserPrincipal principal,
            @Valid @RequestBody PracticeMessageRequest req) {
        return ResponseEntity.ok(practiceService.practice(req.getText()));
    }
}
