package com.scheduler.email.security;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class AuthorizationCacheWarmer implements ApplicationRunner {
    private final AuthorizationCacheService authorizationCacheService;

    @Override
    public void run(@NonNull ApplicationArguments args) {
        try {
            authorizationCacheService.warmCache();
            log.info("Authorization cache warmed successfully on startup");
        } catch (Exception e) {
            // Non-fatal — cache will be populated lazily on first request
            log.warn("Failed to warm authorization cache on startup — will load lazily", e);
        }
    }
}
