package com.github.loa.source.file.service.location;

import com.github.loa.source.file.configuration.FileDocumentSourceConfigurationProperties;
import com.github.loa.source.file.service.FileSourceFactory;
import com.github.loa.source.service.DocumentLocationFactory;
import com.github.loa.url.service.URLConverter;
import io.micrometer.core.instrument.Counter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;

import java.io.BufferedReader;
import java.net.URL;

@Slf4j
@Service
@RequiredArgsConstructor
@ConditionalOnProperty(name = "loa.source.type", havingValue = "file")
public class FileDocumentLocationFactory implements DocumentLocationFactory {

    private final FileDocumentSourceConfigurationProperties fileDocumentSourceConfigurationProperties;
    private final FileSourceFactory fileSourceFactory;
    private final URLConverter urlConverter;

    @Qualifier("processedDocumentLocationCount")
    private final Counter processedDocumentLocationCount;

    @Override
    public Flux<URL> streamLocations() {
        if (fileDocumentSourceConfigurationProperties.getSkipLines() > 0) {
            log.info("Skipping the first " + fileDocumentSourceConfigurationProperties.getSkipLines() + " lines.");
        }

        if (fileDocumentSourceConfigurationProperties.getSkipLines() < 0) {
            throw new IllegalArgumentException("Skip lines shouldn't be negative! It is set to "
                    + fileDocumentSourceConfigurationProperties.getSkipLines() + " lines.");
        }

        final BufferedReader reader = fileSourceFactory.newSourceReader();

        return Flux.fromStream(reader.lines())
                .skip(fileDocumentSourceConfigurationProperties.getSkipLines())
                .doOnNext(line -> processedDocumentLocationCount.increment())
                .skip(2147483647)
                .flatMap(urlConverter::convert);
    }
}
