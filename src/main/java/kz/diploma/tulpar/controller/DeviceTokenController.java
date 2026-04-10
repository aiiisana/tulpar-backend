package kz.diploma.tulpar.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import kz.diploma.tulpar.security.UserPrincipal;
import kz.diploma.tulpar.service.DeviceTokenService;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Device Tokens", description = "Register FCM push tokens")
@RestController
@RequestMapping("/devices")
@RequiredArgsConstructor
public class DeviceTokenController {

    private final DeviceTokenService deviceTokenService;

    @Operation(summary = "Register or refresh an FCM device token",
               description = "Call this on every app launch after obtaining the FCM token. Idempotent.")
    @PostMapping("/token")
    public ResponseEntity<Void> registerToken(
            @Parameter(hidden = true) @AuthenticationPrincipal UserPrincipal principal,
            @Valid @RequestBody RegisterTokenRequest req) {

        deviceTokenService.registerToken(principal.getUid(), req.getToken(), req.getPlatform());
        return ResponseEntity.ok().build();
    }

    @Operation(summary = "Remove a device token",
               description = "Call when the user logs out so they stop receiving push notifications.")
    @DeleteMapping("/token")
    public ResponseEntity<Void> removeToken(
            @Parameter(hidden = true) @AuthenticationPrincipal UserPrincipal principal,
            @RequestParam String token) {

        deviceTokenService.removeStaleToken(token);
        return ResponseEntity.noContent().build();
    }

    // ── Inner DTO ─────────────────────────────────────────────────────────────

    @Data
    public static class RegisterTokenRequest {
        @NotBlank
        private String token;

        @Pattern(regexp = "ios|android|web", message = "platform must be ios, android, or web")
        private String platform = "unknown";
    }
}
