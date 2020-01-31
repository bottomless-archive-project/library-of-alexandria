package com.github.loa.source.file.configuration;

import com.github.loa.source.file.service.location.FileDocumentLocationFactory;
import com.github.loa.source.file.service.FileSourceFactory;
import com.github.loa.source.service.DocumentLocationFactory;
import com.github.loa.url.service.URLConverter;
import io.micrometer.core.instrument.Counter;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.nio.file.Path;

@Configuration
@RequiredArgsConstructor
public class FileDocumentSourceConfiguration {

    @Qualifier("processedDocumentLocationCount")
    private final Counter processedDocumentLocationCount;

    @Bean
    @ConditionalOnProperty(name = "loa.source.type", havingValue = "file")
    public DocumentLocationFactory fileDocumentLocationFactory(
            final FileDocumentSourceConfigurationProperties fileDocumentSourceConfigurationProperties,
            final FileSourceFactory fileSourceFactory, final URLConverter urlConverter) {
        return new FileDocumentLocationFactory(fileSourceFactory, urlConverter,
                Path.of(fileDocumentSourceConfigurationProperties.getLocation()), processedDocumentLocationCount);
    }
}
