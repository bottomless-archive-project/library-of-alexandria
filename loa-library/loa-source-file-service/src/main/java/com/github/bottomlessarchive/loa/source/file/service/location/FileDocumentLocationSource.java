package com.github.bottomlessarchive.loa.source.file.service.location;

import com.github.bottomlessarchive.loa.location.service.id.factory.DocumentLocationIdFactory;
import com.github.bottomlessarchive.loa.source.configuration.DocumentSourceConfiguration;
import com.github.bottomlessarchive.loa.source.source.DocumentLocationSource;
import com.github.bottomlessarchive.loa.location.domain.DocumentLocation;
import com.github.bottomlessarchive.loa.source.file.configuration.FileDocumentSourceConfigurationProperties;
import com.github.bottomlessarchive.loa.source.file.service.FileSourceFactory;
import com.github.bottomlessarchive.loa.type.DocumentTypeCalculator;
import com.github.bottomlessarchive.loa.url.service.encoder.UrlEncoder;
import io.micrometer.core.instrument.Counter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

import java.util.stream.Stream;

@Slf4j
@Service
@RequiredArgsConstructor
@ConditionalOnProperty(name = "loa.source.type", havingValue = "file")
public class FileDocumentLocationSource implements DocumentLocationSource {

    private final DocumentSourceConfiguration documentSourceConfiguration;
    private final FileDocumentSourceConfigurationProperties fileDocumentSourceConfigurationProperties;
    private final DocumentTypeCalculator documentTypeCalculator;
    private final FileSourceFactory fileSourceFactory;
    private final DocumentLocationIdFactory documentLocationIdFactory;
    private final UrlEncoder urlEncoder;

    @Qualifier("processedDocumentLocationCount")
    private final Counter processedDocumentLocationCount;

    @Override
    public Stream<DocumentLocation> streamLocations() {
        if (fileDocumentSourceConfigurationProperties.skipLines() > 0 && log.isInfoEnabled()) {
            log.info("Skipping the first {} lines.", fileDocumentSourceConfigurationProperties.skipLines());
        }

        if (fileDocumentSourceConfigurationProperties.skipLines() < 0) {
            throw new IllegalArgumentException("Skip lines shouldn't be negative! It is set to "
                    + fileDocumentSourceConfigurationProperties.skipLines() + " lines.");
        }

        return fileSourceFactory.newSourceReader()
                .lines()
                .skip(fileDocumentSourceConfigurationProperties.skipLines())
                .peek(url -> processedDocumentLocationCount.increment())
                .flatMap(url -> urlEncoder.encode(url).stream())
                .flatMap(url -> documentTypeCalculator.calculate(url)
                        .map(type ->
                                DocumentLocation.builder()
                                        .id(documentLocationIdFactory.newDocumentLocationId(url))
                                        .location(url)
                                        .sourceName(documentSourceConfiguration.getName())
                                        .type(type)
                                        .build()
                        )
                        .stream()
                );
    }
}
