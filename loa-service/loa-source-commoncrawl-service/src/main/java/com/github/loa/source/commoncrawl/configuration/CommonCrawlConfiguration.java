package com.github.loa.source.commoncrawl.configuration;

import com.github.bottomlessarchive.commoncrawl.WarcLocationFactory;
import com.github.loa.source.commoncrawl.service.*;
import com.github.loa.source.commoncrawl.service.location.CommonCrawlDocumentLocationFactory;
import com.github.loa.source.commoncrawl.service.webpage.WebPageFactory;
import com.github.loa.source.service.DocumentLocationFactory;
import com.github.loa.url.service.URLConverter;
import io.micrometer.core.instrument.Counter;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.net.URL;
import java.util.List;
import java.util.stream.Collectors;

@Configuration
@RequiredArgsConstructor
@ConditionalOnProperty(name = "loa.source.type", havingValue = "common-crawl")
public class CommonCrawlConfiguration {

    @Qualifier("processedDocumentLocationCount")
    private final Counter processedDocumentLocationCount;

    @Bean
    public DocumentLocationFactory warcDocumentLocationFactory(final WarcRecordParser warcRecordParser,
            final WarcFluxFactory warcFluxFactory, final WebPageFactory webPageFactory, final URLConverter urlConverter,
            final WarcLocationFactory warcLocationFactory,
            final CommonCrawlDocumentSourceConfigurationProperties commonCrawlDocumentSourceConfigurationProperties) {
        final List<URL> paths = warcLocationFactory
                .newUrls(commonCrawlDocumentSourceConfigurationProperties.getCrawlId()).stream()
                .skip(commonCrawlDocumentSourceConfigurationProperties.getWarcId())
                .collect(Collectors.toList());

        return new CommonCrawlDocumentLocationFactory(warcRecordParser, warcFluxFactory, webPageFactory,
                urlConverter, paths, processedDocumentLocationCount, commonCrawlDocumentSourceConfigurationProperties);
    }

    @Bean
    public WarcLocationFactory warcLocationFactory() {
        return new WarcLocationFactory();
    }
}
