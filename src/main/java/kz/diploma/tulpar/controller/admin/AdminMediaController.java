package kz.diploma.tulpar.controller.admin;

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
@RestController
@RequestMapping("/admin/media")
@RequiredArgsConstructor
public class AdminMediaController {

    private final MinioService minioService;

    /**
     * POST /admin/media/upload
     * Uploads a media file and returns the stored metadata including the URL.
     *
     * Multipart form fields:
     *   file      — binary file
     *   mediaType — AUDIO | VIDEO | IMAGE
     */
    @PostMapping(value = "/upload", consumes = org.springframework.http.MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<MediaUploadResponse> upload(
            @RequestParam("file") MultipartFile file,
            @RequestParam("mediaType") MediaType mediaType,
            @AuthenticationPrincipal UserPrincipal principal) {

        MediaUploadResponse response = minioService.upload(file, mediaType, principal.getUid());
        return ResponseEntity.ok(response);
    }
}
