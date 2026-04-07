package kz.diploma.tulpar.dto.response;

import kz.diploma.tulpar.domain.enums.MediaType;
import lombok.Builder;
import lombok.Data;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.UUID;

@Data
@Builder @NoArgsConstructor @AllArgsConstructor
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
