package com.github.bottomlessarchive.loa.source.file.configuration;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class GeneratorStatisticsConfiguration {

    @Bean
    public Counter processedDocumentLocationCount(final MeterRegistry meterRegistry) {
        return Counter.builder("generator.processed-document-location-count")
                .tag("printed-name", "processed document location count")
                .baseUnit("urls")
                .register(meterRegistry);
    }
}
