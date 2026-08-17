package ru.putevodika.routing.config;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;

@Configuration
@EnableConfigurationProperties(OsrmProperties.class)
public class OsrmConfig {

    @Bean
    public RestClient osrmRestClient(
            RestClient.Builder builder,
            OsrmProperties properties
    ) {
        return builder
                .baseUrl(properties.baseUrl())
                .build();
    }
}