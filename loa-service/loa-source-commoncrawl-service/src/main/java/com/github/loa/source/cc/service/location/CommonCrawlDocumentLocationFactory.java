package com.github.loa.source.cc.service.location;

import com.github.loa.source.cc.service.WarcDownloader;
import com.github.loa.source.cc.service.WarcFluxFactory;
import com.github.loa.source.cc.service.WarcRecordParser;
import com.github.loa.source.service.DocumentLocationFactory;
import com.github.loa.url.service.URLConverter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Flux;

import java.net.URL;
import java.util.List;

/**
 * A {@link DocumentLocationFactory} that generates locations for parsable items. The items are collected from the
 * <a href="https://commoncrawl.org/the-data/get-started/">Common Crawl</a> corpus.
 */
@Slf4j
@RequiredArgsConstructor
public class CommonCrawlDocumentLocationFactory implements DocumentLocationFactory {

    private final WarcDownloader warcDownloader;
    private final WarcRecordParser warcRecordParser;
    private final WarcFluxFactory warcFluxFactory;
    private final URLConverter urlConverter;
    private final List<String> paths;

    @Override
    public Flux<URL> streamLocations() {
        return Flux.fromIterable(paths)
                .flatMap(warcDownloader::downloadWarcFile)
                .flatMap(warcFluxFactory::buildWarcRecordFlux)
                .flatMap(warcRecordParser::parseUrlsFromRecord)
                .flatMap(urlConverter::convert);
    }
}
