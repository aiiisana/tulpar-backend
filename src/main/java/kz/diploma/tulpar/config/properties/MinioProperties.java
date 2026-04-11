package kz.diploma.tulpar.config.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties(prefix = "minio")
public class MinioProperties {

    private String endpoint;
    private String accessKey;
    private String secretKey;
    private String bucketName;

    /**
     * Optional public base URL for the bucket (e.g. Cloudflare R2 public domain
     * or a custom domain). When set, files are served via direct public URLs
     * instead of presigned URLs.
     *
     * Example: {@code https://pub-xxxx.r2.dev} or {@code https://media.yourdomain.com}
     */
    private String publicUrl;

    /** Presigned URL validity in seconds (used only when publicUrl is not set). */
    private int presignedUrlExpiry = 3600;
}
