package com.scheduler.email.common;

import com.scheduler.email.security.AuthorizationCacheService;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class CacheInitializer {

    private final AuthorizationCacheService authorizationCacheService;

    @EventListener(ApplicationReadyEvent.class)
    public void initialize() {
        authorizationCacheService.updateAllCaches();
    }
}

