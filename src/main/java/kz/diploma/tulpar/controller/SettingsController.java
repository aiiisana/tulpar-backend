package kz.diploma.tulpar.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import kz.diploma.tulpar.dto.response.SettingsResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@Tag(name = "Settings", description = "App settings and feature flags")
@RestController
public class SettingsController {

    @Operation(summary = "Get app settings", description = "Returns static app config: support email, terms and privacy URLs.")
    @ApiResponse(responseCode = "200", description = "Settings returned")
    @GetMapping("/settings")
    public ResponseEntity<SettingsResponse> getSettings() {
        return ResponseEntity.ok(SettingsResponse.builder()
                .supportEmail("support@tulpar.kz")
                .termsUrl("https://tulpar.kz/terms")
                .privacyPolicyUrl("https://tulpar.kz/privacy")
                .appVersion("1.0.0")
                .build());
    }

    @Operation(summary = "Conversation club status", description = "Returns coming-soon status for the Conversation Club feature.")
    @ApiResponse(responseCode = "200", description = "Status returned")
    @GetMapping("/conversation-club")
    public ResponseEntity<Map<String, String>> conversationClub() {
        return ResponseEntity.ok(Map.of(
                "status", "coming_soon",
                "message", "Conversation club will be available soon"
        ));
    }
}
