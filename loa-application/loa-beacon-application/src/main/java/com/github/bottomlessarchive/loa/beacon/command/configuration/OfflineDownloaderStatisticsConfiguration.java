package com.github.bottomlessarchive.loa.beacon.command.configuration;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OfflineDownloaderStatisticsConfiguration {

    @Bean
    public Counter processedDocumentCount(final MeterRegistry meterRegistry) {
        return Counter.builder("downloader.processed-document-count")
                .tag("printed-name", "processed document count")
                .baseUnit("documents")
                .register(meterRegistry);
    }
}
