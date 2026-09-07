package com.scheduler.email.configurations;

import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;

/**
 * Enables Spring's @Async support so mail-sending happens on a separate thread
 * and never blocks the caller (e.g. your auth controller).
 * <p>
 * Thread-pool properties are configured via application.yml:
 * spring.task.execution.pool.*
 */
@EnableAsync
@Configuration
public class AsyncConfig {
    // Spring Boot auto-configures a ThreadPoolTaskExecutor from application.yml.
    // Define a custom @Bean Executor here only if you need fine-grained control.
}
