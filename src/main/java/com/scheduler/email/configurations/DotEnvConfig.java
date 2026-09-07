package com.scheduler.email.configurations;

import io.github.cdimascio.dotenv.Dotenv;
import jakarta.annotation.PostConstruct;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;

@Slf4j
@Configuration
@AllArgsConstructor
public class DotEnvConfig {
    private final Environment environment;

    @PostConstruct
    public void logActiveProfile() {
        String[] activeProfiles = environment.getActiveProfiles();
        if (activeProfiles.length > 0) {
            log.info("------------------------------------------------");
            log.info("Active Profile(s): {}", String.join(", ", activeProfiles));
            log.info("------------------------------------------------");
        } else {
            log.warn("No active profile set. Falling back to default profile.");
        }
    }

    /**
     * Loads .env file and sets entries as system properties.
     * Called statically from ServerApplication.main() before Spring context boots.
     */
    public static void loadDotEnv() {
        try {

            Dotenv dotenv = Dotenv.configure()
                    .ignoreIfMissing()
                    .load();

            dotenv.entries().forEach(entry ->
                    System.setProperty(entry.getKey(), entry.getValue())
            );

            log.info(".env file loaded successfully. {} entries set as system properties.",
                    dotenv.entries().size());

        } catch (Exception e) {
            log.warn("Could not load .env file: {}. Using system environment variables.",
                    e.getMessage());
        }
    }
}
