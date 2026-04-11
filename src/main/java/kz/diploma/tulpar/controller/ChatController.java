package kz.diploma.tulpar.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import kz.diploma.tulpar.dto.request.SendMessageRequest;
import kz.diploma.tulpar.dto.response.ChatMessageResponse;
import kz.diploma.tulpar.dto.response.PageResponse;
import kz.diploma.tulpar.security.UserPrincipal;
import kz.diploma.tulpar.service.ChatService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@Tag(name = "AI Chat", description = "Kazakh language tutor AI assistant")
@RestController
@RequestMapping("/chat")
@RequiredArgsConstructor
public class ChatController {

    private final ChatService chatService;

    @Operation(summary = "Send a message to the AI tutor",
            description = "Sends a message and returns the AI assistant's response. Conversation history is stored.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "AI response returned"),
            @ApiResponse(responseCode = "400", description = "Empty message")
    })
    @PostMapping("/message")
    public ResponseEntity<ChatMessageResponse> sendMessage(
            @AuthenticationPrincipal UserPrincipal principal,
            @Valid @RequestBody SendMessageRequest req) {

        return ResponseEntity.ok(
                chatService.sendMessage(principal.getUid(), req.getMessage())
        );
    }

    @Operation(summary = "Get chat history", description = "Returns paginated conversation history, newest first.")
    @ApiResponse(responseCode = "200", description = "History returned")
    @GetMapping("/history")
    public ResponseEntity<PageResponse<ChatMessageResponse>> getHistory(
            @Parameter(hidden = true) @AuthenticationPrincipal UserPrincipal principal,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(chatService.getHistory(principal.getUid(), page, size));
    }
}
