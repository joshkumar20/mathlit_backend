package com.mathlit.backend.config;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

import java.io.ByteArrayInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Base64;

@Configuration
public class FirebaseConfig {

    @Value("${firebase.service-account-path:serviceAccountKey.json}")
    private String serviceAccountPath;

    // Railway pe yeh env variable mein JSON base64 encoded hoga
    @Value("${FIREBASE_SERVICE_ACCOUNT:}")
    private String firebaseServiceAccountBase64;

    @PostConstruct
    public void init() throws IOException {
        if (FirebaseApp.getApps().isEmpty()) {
            InputStream credentialsStream;

            if (firebaseServiceAccountBase64 != null && !firebaseServiceAccountBase64.isEmpty()) {
                // Production (Railway): env variable se read karo
                byte[] decoded = Base64.getDecoder().decode(firebaseServiceAccountBase64);
                credentialsStream = new ByteArrayInputStream(decoded);
            } else {
                // Local dev: file se read karo
                credentialsStream = new FileInputStream(serviceAccountPath);
            }

            FirebaseOptions options = FirebaseOptions.builder()
                    .setCredentials(GoogleCredentials.fromStream(credentialsStream))
                    .build();
            FirebaseApp.initializeApp(options);
        }
    }
}
