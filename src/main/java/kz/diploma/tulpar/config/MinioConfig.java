package kz.diploma.tulpar.config;

import io.minio.MinioClient;
import kz.diploma.tulpar.config.properties.MinioProperties;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Creates and configures the S3-compatible storage client (MinIO / Cloudflare R2).
 *
 * <p>For Cloudflare R2, buckets must be created in the Cloudflare Dashboard —
 * bucket creation via API is not supported, so no auto-creation is attempted here.
 */
@Slf4j
@Configuration
@RequiredArgsConstructor
public class MinioConfig {

    private final MinioProperties minioProperties;

    @Bean
    public MinioClient minioClient() {
        MinioClient client = MinioClient.builder()
                .endpoint(minioProperties.getEndpoint())
                .credentials(minioProperties.getAccessKey(), minioProperties.getSecretKey())
                .build();

        log.info("S3-compatible client initialised. Endpoint: {}, Bucket: {}",
                minioProperties.getEndpoint(), minioProperties.getBucketName());
        return client;
    }
}
