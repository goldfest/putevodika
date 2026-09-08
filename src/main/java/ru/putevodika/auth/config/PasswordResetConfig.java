package ru.putevodika.auth.config;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executor;

@Configuration(proxyBeanMethods = false)
@EnableAsync
@EnableConfigurationProperties(
        PasswordResetProperties.class
)
public class PasswordResetConfig {

    @Bean(name = "passwordResetExecutor")
    public Executor passwordResetExecutor() {
        ThreadPoolTaskExecutor executor =
                new ThreadPoolTaskExecutor();

        executor.setCorePoolSize(2);
        executor.setMaxPoolSize(4);
        executor.setQueueCapacity(100);
        executor.setThreadNamePrefix(
                "password-reset-mail-"
        );
        executor.initialize();

        return executor;
    }
}
