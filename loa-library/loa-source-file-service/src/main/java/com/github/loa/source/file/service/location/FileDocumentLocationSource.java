package com.github.loa.source.file.service.location;

import com.github.loa.location.domain.link.StringLink;
import com.github.loa.source.source.DocumentLocationSource;
import com.github.loa.location.domain.DocumentLocation;
import com.github.loa.source.configuration.DocumentSourceConfiguration;
import com.github.loa.source.file.configuration.FileDocumentSourceConfigurationProperties;
import com.github.loa.source.file.service.FileSourceFactory;
import io.micrometer.core.instrument.Counter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;

@Slf4j
@Service
@RequiredArgsConstructor
@ConditionalOnProperty(name = "loa.source.type", havingValue = "file")
public class FileDocumentLocationSource implements DocumentLocationSource {

    private final DocumentSourceConfiguration documentSourceConfiguration;
    private final FileDocumentSourceConfigurationProperties fileDocumentSourceConfigurationProperties;
    private final BufferedReaderAdapter adapter;
    private final FileSourceFactory fileSourceFactory;

    @Qualifier("processedDocumentLocationCount")
    private final Counter processedDocumentLocationCount;

    @Override
    public Flux<DocumentLocation> streamLocations() {
        if (fileDocumentSourceConfigurationProperties.getSkipLines() > 0 && log.isInfoEnabled()) {
            log.info("Skipping the first {} lines.", fileDocumentSourceConfigurationProperties.getSkipLines());
        }

        if (fileDocumentSourceConfigurationProperties.getSkipLines() < 0) {
            throw new IllegalArgumentException("Skip lines shouldn't be negative! It is set to "
                    + fileDocumentSourceConfigurationProperties.getSkipLines() + " lines.");
        }

        return Flux.using(fileSourceFactory::newSourceReader, adapter.consume(), adapter.close())
                .skip(fileDocumentSourceConfigurationProperties.getSkipLines())
                .doOnNext(line -> processedDocumentLocationCount.increment())
                .map(link -> DocumentLocation.builder()
                        .location(
                                StringLink.builder()
                                        .link(link)
                                        .build()
                        )
                        .sourceName(documentSourceConfiguration.getName())
                        .build()
                );
    }
}
