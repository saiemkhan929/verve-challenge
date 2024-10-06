package com.verve.challenge.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

@Configuration
public class ThreadPoolConfig {

    @Bean
    public ThreadPoolTaskExecutor taskExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(500);
        executor.setMaxPoolSize(1000);
        executor.setQueueCapacity(2000);
        executor.setThreadNamePrefix("verve-thread-");
        executor.initialize();
        System.out.println("Thread Pool Executor bean -  created");
        return executor;
    }
}
