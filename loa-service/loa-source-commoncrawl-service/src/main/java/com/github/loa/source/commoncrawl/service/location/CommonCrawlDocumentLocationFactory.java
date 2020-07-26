package com.github.loa.source.commoncrawl.service.location;

import com.github.loa.source.commoncrawl.service.WarcFluxFactory;
import com.github.loa.source.commoncrawl.service.WarcRecordParser;
import com.github.loa.source.commoncrawl.service.webpage.WebPageFactory;
import com.github.loa.source.service.DocumentLocationFactory;
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
 * A {@link DocumentLocationFactory} that generates locations for parsable items. The items are collected from the
 * <a href="https://commoncrawl.org/the-data/get-started/">Common Crawl</a> corpus.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class CommonCrawlDocumentLocationFactory implements DocumentLocationFactory {

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

    @Override
    public Flux<URL> streamLocations() {
        return Flux.fromIterable(paths)
                .flatMap(warcLocation ->
                        Mono.just(warcLocation)
                                .flatMapMany(warcFluxFactory::buildWarcRecordFlux)
                                .map(webPageFactory::newWebPage)
                                .publishOn(documentLocationParserScheduler)
                                .flatMap(warcRecordParser::parseUrlsFromRecord)
                                .doOnNext(line -> processedDocumentLocationCount.increment())
                                .flatMap(urlConverter::convert)
                                .doOnError(error -> log.error("Failed to download WARC location {}!", warcLocation, error))
                                .retry()
                );
    }
}
