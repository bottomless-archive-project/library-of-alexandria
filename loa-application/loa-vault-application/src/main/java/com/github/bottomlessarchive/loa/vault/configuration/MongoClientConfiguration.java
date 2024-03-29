package com.github.bottomlessarchive.loa.vault.configuration;

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
                    connectionPoolSettings.maxSize(100);
                    connectionPoolSettings.minSize(100);
                    connectionPoolSettings.maxWaitTime(10, TimeUnit.MINUTES);
                });
    }
}
