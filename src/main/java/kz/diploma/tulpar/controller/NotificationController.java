package kz.diploma.tulpar.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import kz.diploma.tulpar.dto.response.NotificationResponse;
import kz.diploma.tulpar.dto.response.PageResponse;
import kz.diploma.tulpar.security.UserPrincipal;
import kz.diploma.tulpar.service.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@Tag(name = "Notifications", description = "In-app notifications")
@RestController
@RequestMapping("/notifications")
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationService notificationService;

    @Operation(summary = "Get my notifications", description = "Returns paginated notifications, newest first.")
    @ApiResponse(responseCode = "200", description = "Notifications returned")
    @GetMapping
    public ResponseEntity<PageResponse<NotificationResponse>> getNotifications(
            @Parameter(hidden = true) @AuthenticationPrincipal UserPrincipal principal,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(notificationService.findByUser(principal.getUid(), page, size));
    }

    @Operation(summary = "Mark notification as read")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Marked as read"),
        @ApiResponse(responseCode = "404", description = "Notification not found or not owned by user", content = @Content)
    })
    @PatchMapping("/{id}/read")
    public ResponseEntity<NotificationResponse> markRead(
            @Parameter(hidden = true) @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable UUID id) {
        return ResponseEntity.ok(notificationService.markRead(principal.getUid(), id));
    }
}
