package kz.diploma.tulpar.controller.admin;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import kz.diploma.tulpar.domain.enums.MediaType;
import kz.diploma.tulpar.dto.response.MediaUploadResponse;
import kz.diploma.tulpar.security.UserPrincipal;
import kz.diploma.tulpar.service.MinioService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

/**
 * Admin/Content-Manager endpoints for uploading media files to MinIO.
 * Security: roles ADMIN or CONTENT_MANAGER (enforced in SecurityConfig).
 */
@Tag(name = "Admin — Media", description = "Media file upload to MinIO (ADMIN or CONTENT_MANAGER)")
@RestController
@RequestMapping("/admin/media")
@RequiredArgsConstructor
public class AdminMediaController {

    private final MinioService minioService;

    @Operation(
        summary = "Upload media file",
        description = "Uploads a binary file (audio, video, or image) to MinIO object storage. " +
                      "Returns stored metadata including a presigned download URL."
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "File uploaded, metadata returned"),
        @ApiResponse(responseCode = "400", description = "Missing or invalid multipart fields", content = @Content),
        @ApiResponse(responseCode = "401", description = "Missing or invalid Firebase token", content = @Content),
        @ApiResponse(responseCode = "403", description = "Insufficient role", content = @Content),
        @ApiResponse(responseCode = "500", description = "MinIO upload error", content = @Content)
    })
    @PostMapping(value = "/upload", consumes = org.springframework.http.MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<MediaUploadResponse> upload(
            @Parameter(description = "Binary file to upload", required = true)
            @RequestParam("file") MultipartFile file,
            @Parameter(description = "Type of media: AUDIO, VIDEO, or IMAGE", required = true,
                       schema = @io.swagger.v3.oas.annotations.media.Schema(implementation = MediaType.class))
            @RequestParam("mediaType") MediaType mediaType,
            @Parameter(hidden = true) @AuthenticationPrincipal UserPrincipal principal) {

        MediaUploadResponse response = minioService.upload(file, mediaType, principal.getUid());
        return ResponseEntity.ok(response);
    }
}
