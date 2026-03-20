package com.example.backend.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.cache.CacheManager;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
@org.springframework.context.annotation.Profile("!test")
public class CacheEvictOnStartup {

    private final CacheManager cacheManager;

    @EventListener(ApplicationReadyEvent.class)
    public void evictToursCache() {
        var cache = cacheManager.getCache("tours");
        if (cache != null) {
            cache.clear();
            log.info("Tours cache cleared on startup");
        }
    }
}
