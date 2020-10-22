package com.github.loa.source.commoncrawl.service.location;

import com.github.loa.source.commoncrawl.configuration.CommonCrawlDocumentSourceConfigurationProperties;
import com.github.loa.source.source.DocumentLocationSource;
import com.github.loa.location.domain.DocumentLocation;
import com.github.loa.source.commoncrawl.service.WarcFluxFactory;
import com.github.loa.source.commoncrawl.service.WarcRecordParser;
import com.github.loa.source.commoncrawl.service.webpage.WebPageFactory;
import com.github.loa.source.configuration.DocumentSourceConfiguration;
import com.github.loa.url.service.URLConverter;
import io.micrometer.core.instrument.Counter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Scheduler;

import java.net.URL;
import java.util.List;

/**
 * A {@link DocumentLocationSource} that generates locations for parsable items. The items are collected from the
 * <a href="https://commoncrawl.org/the-data/get-started/">Common Crawl</a> corpus.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class CommonCrawlDocumentLocationSource implements DocumentLocationSource {

    private final DocumentSourceConfiguration documentSourceConfiguration;
    private final WarcRecordParser warcRecordParser;
    private final WarcFluxFactory warcFluxFactory;
    private final WebPageFactory webPageFactory;
    private final URLConverter urlConverter;

    @Qualifier("documentLocationParserScheduler")
    private final Scheduler documentLocationParserScheduler;

    @Qualifier("processedDocumentLocationCount")
    private final Counter processedDocumentLocationCount;

    @Qualifier("warcLocations")
    private final List<URL> paths;

    private final CommonCrawlDocumentSourceConfigurationProperties commonCrawlDocumentSourceConfigurationProperties;

    @Override
    public Flux<DocumentLocation> streamLocations() {
        return Flux.fromIterable(paths)
                .flatMap(this::processWarcLocation, commonCrawlDocumentSourceConfigurationProperties.getMaximumRecordProcessors());
    }

    private Flux<DocumentLocation> processWarcLocation(final URL warcLocation) {
        log.info("Started to process location: {}", warcLocation);

        return Mono.just(warcLocation)
                .flatMapMany(warcFluxFactory::buildWarcRecordFlux)
                .map(webPageFactory::newWebPage)
                .subscribeOn(documentLocationParserScheduler)
                .flatMap(warcRecordParser::parseUrlsFromRecord)
                .doOnNext(line -> processedDocumentLocationCount.increment())
                .flatMap(this::buildLocation)
                .doOnError(error -> handleError(warcLocation, error))
                .retry();
    }

    private Mono<DocumentLocation> buildLocation(final String location) {
        return urlConverter.convert(location)
                .map(url -> DocumentLocation.builder()
                        .location(url)
                        .sourceName(documentSourceConfiguration.getName())
                        .build()
                );
    }

    private void handleError(final URL warcLocation, final Throwable error) {
        log.error("Failed to download WARC location {}!", warcLocation, error);
    }
}
