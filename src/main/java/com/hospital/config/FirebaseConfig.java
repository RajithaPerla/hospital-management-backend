package com.hospital.config;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

/**
 * Initializes the Firebase Admin SDK once at startup so the security
 * filter can verify Google ID tokens issued to the Angular frontend.
 *
 * On Render, paste the *entire contents* of your Firebase service-account
 * JSON file into an environment variable named FIREBASE_SERVICE_ACCOUNT_JSON.
 * Nothing sensitive is ever committed to the repo.
 */
@Component
public class FirebaseConfig {

    private static final Logger log = LoggerFactory.getLogger(FirebaseConfig.class);

    @Value("${firebase.service-account-json}")
    private String serviceAccountJson;

    @PostConstruct
    public void init() {
        try {
            if (!FirebaseApp.getApps().isEmpty()) {
                return;
            }
            if (serviceAccountJson == null || serviceAccountJson.isBlank()) {
                log.warn("FIREBASE_SERVICE_ACCOUNT_JSON is not set. Firebase Admin SDK will not be initialized.");
                return;
            }

            try (InputStream in = new ByteArrayInputStream(
                    serviceAccountJson.getBytes(StandardCharsets.UTF_8))) {
                FirebaseOptions options = FirebaseOptions.builder()
                        .setCredentials(GoogleCredentials.fromStream(in))
                        .build();
                FirebaseApp.initializeApp(options);
                log.info("Firebase Admin SDK initialized successfully.");
            }
        } catch (Exception e) {
            // Fail loudly at startup rather than silently accepting no auth.
            throw new IllegalStateException("Failed to initialize Firebase Admin SDK", e);
        }
    }
}
