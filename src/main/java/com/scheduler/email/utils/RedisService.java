package com.scheduler.email.utils;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
public class RedisService {
    private final RedisTemplate<String, Object> redisTemplate;

    /**
     * This method is used to save data into cache
     */
    public void set(
            String key,
            Object value
    ) {
        redisTemplate.opsForValue()
                .set(key, value);
    }

    /**
     * This method is used to save data into cache with ttl
     */
    public void set(
            String key,
            Object value,
            long timeoutSeconds
    ) {
        redisTemplate.opsForValue()
                .set(
                        key,
                        value,
                        timeoutSeconds,
                        TimeUnit.SECONDS
                );
    }

    /**
     * This method is used to save data into cache for a particular duration
     */
    public void set(
            String key,
            Object value,
            Duration duration
    ) {
        redisTemplate.opsForValue()
                .set(
                        key,
                        value,
                        duration
                );
    }

    /**
     * This method is used to get data from cache using key
     */
    public Optional<Object> get(String key) {
        return Optional.ofNullable(
                redisTemplate.opsForValue()
                        .get(key)
        );
    }

    /**
     * This method is used to get data from cache using key
     */
    public boolean delete(String key) {

        Boolean deleted =
                redisTemplate.delete(key);

        return Boolean.TRUE.equals(deleted);
    }
}
