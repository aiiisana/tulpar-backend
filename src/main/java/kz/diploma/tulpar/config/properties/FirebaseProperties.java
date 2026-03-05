package kz.diploma.tulpar.config.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties(prefix = "firebase")
public class FirebaseProperties {

    /** Classpath or file-system path to the service account JSON. */
    private String serviceAccountPath;

    private String projectId;
}
