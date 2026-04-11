package kz.diploma.tulpar.service;

import io.minio.*;
import io.minio.http.Method;
import kz.diploma.tulpar.config.properties.MinioProperties;
import kz.diploma.tulpar.domain.entity.MediaFile;
import kz.diploma.tulpar.domain.enums.MediaType;
import kz.diploma.tulpar.dto.response.MediaUploadResponse;
import kz.diploma.tulpar.exception.MediaUploadException;
import kz.diploma.tulpar.repository.MediaFileRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.UUID;
import java.util.concurrent.TimeUnit;

/**
 * Handles all S3-compatible storage operations (MinIO / Cloudflare R2).
 *
 * Upload flow:
 *   1. Receive MultipartFile from admin controller
 *   2. Generate a unique object key (media-type prefix + UUID + original extension)
 *   3. Stream file to the storage backend
 *   4. Persist metadata to the media_files table
 *   5. Return either a public URL (if {@code minio.public-url} is configured)
 *      or a presigned GET URL valid for {@code minio.presigned-url-expiry} seconds
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class MinioService {

    private final MinioClient minioClient;
    private final MinioProperties minioProperties;
    private final MediaFileRepository mediaFileRepository;

    @Transactional
    public MediaUploadResponse upload(MultipartFile file, MediaType mediaType, String uploadedByUid) {
        String originalFilename = file.getOriginalFilename() != null
                ? file.getOriginalFilename() : "unknown";
        String extension = extractExtension(originalFilename);
        String objectKey  = mediaType.name().toLowerCase() + "/" + UUID.randomUUID() + extension;
        String bucket     = minioProperties.getBucketName();

        try {
            minioClient.putObject(PutObjectArgs.builder()
                    .bucket(bucket)
                    .object(objectKey)
                    .stream(file.getInputStream(), file.getSize(), -1)
                    .contentType(file.getContentType())
                    .build());
        } catch (Exception ex) {
            throw new MediaUploadException("Failed to upload file to MinIO: " + originalFilename, ex);
        }

        String presignedUrl = resolveFileUrl(bucket, objectKey);

        MediaFile mediaFile = MediaFile.builder()
                .mediaType(mediaType)
                .originalFilename(originalFilename)
                .objectKey(objectKey)
                .url(presignedUrl)
                .contentType(file.getContentType())
                .sizeBytes(file.getSize())
                .uploadedBy(uploadedByUid)
                .build();

        MediaFile saved = mediaFileRepository.save(mediaFile);
        log.info("Media uploaded: {} -> {}", originalFilename, objectKey);

        return MediaUploadResponse.builder()
                .id(saved.getId())
                .mediaType(saved.getMediaType())
                .originalFilename(saved.getOriginalFilename())
                .objectKey(saved.getObjectKey())
                .url(saved.getUrl())
                .contentType(saved.getContentType())
                .sizeBytes(saved.getSizeBytes())
                .uploadedAt(saved.getUploadedAt())
                .build();
    }

    /**
     * Returns the best URL for accessing the object.
     * <ul>
     *   <li>If {@code minio.public-url} is set (Cloudflare R2 public bucket or custom domain),
     *       returns a stable public URL — no expiry, no signing overhead.</li>
     *   <li>Otherwise falls back to a presigned GET URL valid for
     *       {@code minio.presigned-url-expiry} seconds.</li>
     * </ul>
     */
    public String resolveFileUrl(String bucket, String objectKey) {
        String publicBase = minioProperties.getPublicUrl();
        if (publicBase != null && !publicBase.isBlank()) {
            // e.g. https://pub-xxxx.r2.dev/audio/uuid.mp3
            return publicBase.stripTrailing() + "/" + objectKey;
        }
        return generatePresignedUrl(bucket, objectKey);
    }

    public String generatePresignedUrl(String bucket, String objectKey) {
        try {
            return minioClient.getPresignedObjectUrl(GetPresignedObjectUrlArgs.builder()
                    .method(Method.GET)
                    .bucket(bucket)
                    .object(objectKey)
                    .expiry(minioProperties.getPresignedUrlExpiry(), TimeUnit.SECONDS)
                    .build());
        } catch (Exception ex) {
            throw new MediaUploadException("Failed to generate presigned URL for: " + objectKey, ex);
        }
    }

    private String extractExtension(String filename) {
        int dot = filename.lastIndexOf('.');
        return dot >= 0 ? filename.substring(dot) : "";
    }
}
