package com.github.loa.downloader;

import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.logging.LoggingMeterRegistry;
import io.micrometer.core.instrument.logging.LoggingRegistryConfig;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

/**
 * The runner class of the downloader application.
 */
@Slf4j
@SpringBootApplication(scanBasePackages = "com.github.loa")
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
