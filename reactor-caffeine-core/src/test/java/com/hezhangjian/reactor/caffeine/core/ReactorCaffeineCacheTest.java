package com.hezhangjian.reactor.caffeine.core;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import reactor.test.StepVerifier;

import java.time.Duration;

class ReactorCaffeineCacheTest {
    private ReactorCaffeineCache<String, String> cache;

    @BeforeEach
    void setUp() {
        cache = ReactorCaffeineCache.builder()
                .maximumSize(100)
                .expireAfterWrite(Duration.ofMinutes(5))
                .build();
    }

    @Test
    void getIfPresent_shouldReturnEmptyMono_whenKeyDoesNotExist() {
        StepVerifier.create(cache.getIfPresent("non-existent-key"))
                .verifyComplete();
    }
}
