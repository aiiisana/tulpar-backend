package kz.diploma.tulpar.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import kz.diploma.tulpar.dto.request.UpdateProfileRequest;
import kz.diploma.tulpar.dto.response.ProfileResponse;
import kz.diploma.tulpar.security.UserPrincipal;
import kz.diploma.tulpar.service.ProfileService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@Tag(name = "Profile", description = "User profile and settings")
@RestController
@RequestMapping("/profile")
@RequiredArgsConstructor
public class ProfileController {

    private final ProfileService profileService;

    @Operation(summary = "Get my profile", description = "Returns the authenticated user's full profile including streak and XP stats.")
    @ApiResponse(responseCode = "200", description = "Profile returned")
    @GetMapping
    public ResponseEntity<ProfileResponse> getProfile(
            @Parameter(hidden = true) @AuthenticationPrincipal UserPrincipal principal) {
        return ResponseEntity.ok(profileService.getProfile(principal.getUid()));
    }

    @Operation(summary = "Update my profile", description = "Updates username, avatar URL, or notification preference. Null fields are ignored.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Profile updated"),
        @ApiResponse(responseCode = "400", description = "Validation error")
    })
    @PutMapping
    public ResponseEntity<ProfileResponse> updateProfile(
            @Parameter(hidden = true) @AuthenticationPrincipal UserPrincipal principal,
            @RequestBody UpdateProfileRequest req) {
        return ResponseEntity.ok(profileService.updateProfile(principal.getUid(), req));
    }

    @Operation(
        summary = "Upload avatar",
        description = "Uploads an image file to R2 storage and sets it as the user's avatar. " +
                      "Accepted MIME types: image/jpeg, image/png, image/webp, etc."
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Avatar uploaded and profile updated"),
        @ApiResponse(responseCode = "400", description = "File is not an image"),
        @ApiResponse(responseCode = "401", description = "Missing or invalid Firebase token")
    })
    @PostMapping(value = "/avatar", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ProfileResponse> uploadAvatar(
            @Parameter(hidden = true) @AuthenticationPrincipal UserPrincipal principal,
            @Parameter(description = "Image file (jpeg, png, webp, etc.)", required = true)
            @RequestParam("file") MultipartFile file) {
        return ResponseEntity.ok(profileService.uploadAvatar(principal.getUid(), file));
    }
}
