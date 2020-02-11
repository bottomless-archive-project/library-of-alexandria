package com.github.loa.vault.configuration;

import org.springframework.boot.autoconfigure.mongo.MongoClientSettingsBuilderCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.TimeUnit;

@Configuration
public class MongoClientConfiguration {

    @Bean
    public MongoClientSettingsBuilderCustomizer mongoClientSettingsBuilderCustomizer() {
        return clientSettingsBuilder -> clientSettingsBuilder
                .applyToConnectionPoolSettings(connectionPoolSettings -> {
                    connectionPoolSettings.maxWaitTime(10, TimeUnit.MINUTES);
                    connectionPoolSettings.maxWaitQueueSize(500);
                });
    }
}
