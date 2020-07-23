package com.github.loa.source.commoncrawl.service.location;

import com.github.loa.source.commoncrawl.service.WarcFluxFactory;
import com.github.loa.source.commoncrawl.service.WarcRecordParser;
import com.github.loa.source.service.DocumentLocationFactory;
import com.github.loa.url.service.URLConverter;
import io.micrometer.core.instrument.Counter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.net.URL;
import java.util.List;

/**
 * A {@link DocumentLocationFactory} that generates locations for parsable items. The items are collected from the
 * <a href="https://commoncrawl.org/the-data/get-started/">Common Crawl</a> corpus.
 */
@Slf4j
@RequiredArgsConstructor
public class CommonCrawlDocumentLocationFactory implements DocumentLocationFactory {

    private final WarcRecordParser warcRecordParser;
    private final WarcFluxFactory warcFluxFactory;
    private final URLConverter urlConverter;
    private final List<URL> paths;
    private final Counter processedDocumentLocationCount;

    @Override
    public Flux<URL> streamLocations() {
        return Flux.fromIterable(paths)
                .flatMap(warcLocation ->
                        Mono.just(warcLocation)
                                .flatMapMany(warcFluxFactory::buildWarcRecordFlux)
                                .flatMap(warcRecordParser::parseUrlsFromRecord)
                                .doOnNext(line -> processedDocumentLocationCount.increment())
                                .flatMap(urlConverter::convert)
                                .doOnError(error -> log.error("Failed to download WARC location {}!", warcLocation, error))
                                .retry()
                );
    }
}
