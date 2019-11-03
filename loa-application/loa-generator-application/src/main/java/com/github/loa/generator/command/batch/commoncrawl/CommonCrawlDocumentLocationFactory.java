package com.github.loa.generator.command.batch.commoncrawl;

import com.github.loa.generator.command.batch.DocumentLocationFactory;
import com.github.loa.generator.command.batch.commoncrawl.warc.WarcDownloader;
import com.github.loa.generator.command.batch.commoncrawl.warc.WarcFluxFactory;
import com.github.loa.generator.command.batch.commoncrawl.warc.WarcRecordParser;
import com.github.loa.source.cc.configuration.CommonCrawlDocumentSourceConfiguration;
import com.github.loa.source.cc.service.WarcPathFactory;
import com.github.loa.url.service.URLConverter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;

import java.net.URL;
import java.util.List;
import java.util.stream.Collectors;

/**
 * A {@link DocumentLocationFactory} that generates locations for parsable items. The items are collected from the
 * <a href="https://commoncrawl.org/the-data/get-started/">Common Crawl</a> corpus.
 */
@Slf4j
@Service
@RequiredArgsConstructor
@ConditionalOnProperty(name = "loa.source.type", havingValue = "common-crawl")
public class CommonCrawlDocumentLocationFactory implements DocumentLocationFactory {

    private final WarcDownloader warcDownloader;
    private final WarcRecordParser warcRecordParser;
    private final WarcPathFactory warcPathFactory;
    private final WarcFluxFactory warcFluxFactory;
    private final URLConverter urlConverter;
    private final CommonCrawlDocumentSourceConfiguration commonCrawlDocumentSourceConfiguration;

    @Override
    public Flux<URL> streamLocations() {
        final List<String> paths = warcPathFactory.newPaths(commonCrawlDocumentSourceConfiguration.getCrawlId()).stream()
                .skip(commonCrawlDocumentSourceConfiguration.getWarcId())
                .collect(Collectors.toList());

        return Flux.fromIterable(paths)
                .flatMap(warcDownloader::downloadWarcFile)
                .flatMap(warcFluxFactory::buildWarcRecordFlux)
                .flatMap(warcRecordParser::parseUrlsFromRecord)
                .flatMap(urlConverter::convert);
    }
}
