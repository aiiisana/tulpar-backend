package kz.diploma.tulpar.config;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.auth.FirebaseAuth;
import kz.diploma.tulpar.config.properties.FirebaseProperties;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;

import java.io.IOException;
import java.io.InputStream;

/**
 * Initialises the Firebase Admin SDK once at startup.
 * The service account JSON is loaded from the path declared in application.yml.
 * It can be a classpath resource (e.g. "firebase-service-account.json" placed in
 * src/main/resources) or an absolute file-system path (preferred for production
 * where secrets are injected via a mounted volume or secret manager).
 */
@Slf4j
@Configuration
@RequiredArgsConstructor
public class FirebaseConfig {

    private final FirebaseProperties firebaseProperties;

    @Bean
    public FirebaseApp firebaseApp() throws IOException {
        if (!FirebaseApp.getApps().isEmpty()) {
            return FirebaseApp.getInstance();
        }

        InputStream serviceAccountStream = resolveServiceAccount();

        FirebaseOptions options = FirebaseOptions.builder()
                .setCredentials(GoogleCredentials.fromStream(serviceAccountStream))
                .setProjectId(firebaseProperties.getProjectId())
                .build();

        log.info("Initializing Firebase Admin SDK for project: {}", firebaseProperties.getProjectId());
        return FirebaseApp.initializeApp(options);
    }

    @Bean
    public FirebaseAuth firebaseAuth(FirebaseApp firebaseApp) {
        return FirebaseAuth.getInstance(firebaseApp);
    }

    private InputStream resolveServiceAccount() throws IOException {
        String path = firebaseProperties.getServiceAccountPath();

        // Try classpath first (useful for local dev with the file in src/main/resources)
        Resource classpath = new ClassPathResource(path);
        if (classpath.exists()) {
            return classpath.getInputStream();
        }

        // Fall back to file-system path (for production Docker volumes / secrets)
        Resource fileSystem = new FileSystemResource(path);
        if (fileSystem.exists()) {
            return fileSystem.getInputStream();
        }

        throw new IllegalStateException(
                "Firebase service account not found at: " + path +
                " (checked classpath and file system)");
    }
}
