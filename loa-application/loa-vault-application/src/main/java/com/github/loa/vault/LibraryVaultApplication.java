package com.github.loa.vault;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.mongo.MongoClientSettingsBuilderCustomizer;
import org.springframework.context.annotation.Bean;

import java.util.concurrent.TimeUnit;

@SpringBootApplication(scanBasePackages = "com.github.loa")
public class LibraryVaultApplication {

    public static void main(final String[] args) {
        SpringApplication.run(LibraryVaultApplication.class, args);
    }

    @Bean
    public MongoClientSettingsBuilderCustomizer mongoClientSettingsBuilderCustomizer() {
        return clientSettingsBuilder -> clientSettingsBuilder
                .applyToConnectionPoolSettings(connectionPoolSettings -> {
                    connectionPoolSettings.maxWaitTime(10, TimeUnit.MINUTES);
                    connectionPoolSettings.maxWaitQueueSize(1000);
                });
    }
}
