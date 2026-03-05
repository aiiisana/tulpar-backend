package kz.diploma.tulpar.domain.entity;

import jakarta.persistence.*;
import kz.diploma.tulpar.domain.enums.MediaType;
import lombok.*;

import java.time.Instant;
import java.util.UUID;

/**
 * Tracks every media file uploaded to MinIO.
 * The object key is the path used inside the MinIO bucket.
 */
@Entity
@Table(name = "media_files")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MediaFile {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    @Enumerated(EnumType.STRING)
    @Column(name = "media_type", nullable = false, length = 16)
    private MediaType mediaType;

    /** Original filename as uploaded by the admin. */
    @Column(name = "original_filename", nullable = false, length = 512)
    private String originalFilename;

    /** The key (path) of the object inside the MinIO bucket. */
    @Column(name = "object_key", nullable = false, unique = true, length = 1024)
    private String objectKey;

    /** Full URL to access the file (presigned or public depending on bucket policy). */
    @Column(name = "url", nullable = false, length = 2048)
    private String url;

    /** MIME type e.g. audio/mpeg, video/mp4, image/jpeg. */
    @Column(name = "content_type", length = 128)
    private String contentType;

    /** File size in bytes. */
    @Column(name = "size_bytes")
    private Long sizeBytes;

    /** Firebase UID of the uploader. */
    @Column(name = "uploaded_by", nullable = false, length = 128)
    private String uploadedBy;

    @Column(name = "uploaded_at", nullable = false, updatable = false)
    private Instant uploadedAt;

    @PrePersist
    protected void onCreate() {
        uploadedAt = Instant.now();
    }
}
