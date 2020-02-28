package com.github.loa.source.file.service.location;

import com.github.loa.source.file.configuration.FileDocumentSourceConfigurationProperties;
import com.github.loa.source.file.service.FileSourceFactory;
import com.github.loa.source.service.DocumentLocationFactory;
import com.github.loa.url.service.URLConverter;
import io.micrometer.core.instrument.Counter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import reactor.core.publisher.Flux;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;
import java.nio.file.Path;

@Slf4j
@RequiredArgsConstructor
public class FileDocumentLocationFactory implements DocumentLocationFactory {

    private final FileDocumentSourceConfigurationProperties fileDocumentSourceConfigurationProperties;
    private final FileSourceFactory fileSourceFactory;
    private final URLConverter urlConverter;
    private final Path filePath;

    @Qualifier("processedDocumentLocationCount")
    private final Counter processedDocumentLocationCount;

    @Override
    public Flux<URL> streamLocations() {
        if (fileDocumentSourceConfigurationProperties.getSkipLines() > 0) {
            log.info("Skipping the first " + fileDocumentSourceConfigurationProperties.getSkipLines() + " lines.");
        }

        if (fileDocumentSourceConfigurationProperties.getSkipLines() < 0) {
            throw new RuntimeException("Skip lines shouldn't be negative! It is set to "
                    + fileDocumentSourceConfigurationProperties.getSkipLines() + " lines.");
        }

        final BufferedReader reader = new BufferedReader(new InputStreamReader(
                fileSourceFactory.newInputStream(filePath)));

        return Flux.fromStream(reader.lines())
                .doOnNext(line -> processedDocumentLocationCount.increment())
                .skip(fileDocumentSourceConfigurationProperties.getSkipLines())
                .flatMap(urlConverter::convert);
    }
}
