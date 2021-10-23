package com.github.bottomlessarchive.loa.downloader.configuration;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class DownloaderStatisticsConfiguration {

    @Bean
    public Counter processedDocumentCount(final MeterRegistry meterRegistry) {
        return Counter.builder("downloader.processed-document-count")
                .tag("printed-name", "processed document count")
                .baseUnit("documents")
                .register(meterRegistry);
    }

    @Bean
    public Counter archivedDocumentCount(final MeterRegistry meterRegistry) {
        return Counter.builder("downloader.archived-document-count")
                .tag("printed-name", "archived document count")
                .baseUnit("documents")
                .register(meterRegistry);
    }
}
