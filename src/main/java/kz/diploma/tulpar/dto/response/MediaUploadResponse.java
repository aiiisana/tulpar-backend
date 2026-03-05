package kz.diploma.tulpar.dto.response;

import kz.diploma.tulpar.domain.enums.MediaType;
import lombok.Builder;
import lombok.Data;

import java.time.Instant;
import java.util.UUID;

@Data
@Builder
public class MediaUploadResponse {

    private UUID id;
    private MediaType mediaType;
    private String originalFilename;
    private String objectKey;
    private String url;
    private String contentType;
    private Long sizeBytes;
    private Instant uploadedAt;
}
