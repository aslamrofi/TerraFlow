package org.springdemo.terraflow.config;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.database.FirebaseDatabase;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.Resource;

import java.io.IOException;
import java.io.InputStream;

@Configuration
public class FirebaseConfig {

    @Value("${firebase.database-url}")
    private String databaseUrl;

    // Spring resolves a String property into a Resource automatically,
    // including the classpath: prefix set in application.properties.
    @Value("${firebase.credentials-path}")
    private Resource credentialsResource;

    @Bean
    public FirebaseApp firebaseApp() throws IOException {
        if (!FirebaseApp.getApps().isEmpty()) {
            return FirebaseApp.getInstance();
        }
        try (InputStream serviceAccount = credentialsResource.getInputStream()) {
            FirebaseOptions options = FirebaseOptions.builder()
                    .setCredentials(GoogleCredentials.fromStream(serviceAccount))
                    .setDatabaseUrl(databaseUrl)
                    .build();
            return FirebaseApp.initializeApp(options);
        }
    }

    @Bean
    public FirebaseDatabase firebaseDatabase(FirebaseApp firebaseApp) {
        return FirebaseDatabase.getInstance(firebaseApp);
    }
}
