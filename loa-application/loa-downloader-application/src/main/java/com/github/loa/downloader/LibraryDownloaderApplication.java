package com.github.loa.downloader;

import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.logging.LoggingMeterRegistry;
import io.micrometer.core.instrument.logging.LoggingRegistryConfig;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.data.mongo.MongoDataAutoConfiguration;
import org.springframework.boot.autoconfigure.data.mongo.MongoRepositoriesAutoConfiguration;
import org.springframework.boot.autoconfigure.mongo.MongoAutoConfiguration;
import org.springframework.context.annotation.Bean;

/**
 * The runner class of the downloader application.
 */
@Slf4j
@SpringBootApplication(scanBasePackages = "com.github.loa", exclude = {MongoAutoConfiguration.class,
        MongoRepositoriesAutoConfiguration.class, MongoDataAutoConfiguration.class})
public class LibraryDownloaderApplication {

    public static void main(final String[] args) {
        SpringApplication.run(LibraryDownloaderApplication.class, args);
    }

    @Bean
    public MeterRegistry meterRegistry() {
        return LoggingMeterRegistry.builder(LoggingRegistryConfig.DEFAULT)
                .loggingSink((str) -> {
                    if (str.startsWith("statistics")) {
                        log.info(str);
                    }
                })
                .build();
    }
}
