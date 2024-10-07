package com.verve.challenge.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

/**
 * Configuration class for setting up the ThreadPoolTaskExecutor bean.
 * This executor is used for handling asynchronous tasks throughout the application.
 */
@Configuration
public class ThreadPoolConfig {

    /**
     * Configures a ThreadPoolTaskExecutor with custom settings for core pool size, max pool size, and queue capacity.
     * This executor will handle asynchronous tasks with a large thread pool to accommodate high concurrency.
     *
     * @return a configured ThreadPoolTaskExecutor instance
     */
    @Bean
    public ThreadPoolTaskExecutor taskExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(1000);
        executor.setMaxPoolSize(2000);
        executor.setQueueCapacity(12000);
        executor.setThreadNamePrefix("verve-thread-");
        executor.initialize();
        System.out.println("Thread Pool Executor bean - created");
        return executor;
    }
}
