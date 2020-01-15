package com.github.loa.source.cc.configuration;

import com.github.loa.source.cc.service.*;
import com.github.loa.source.cc.service.location.CommonCrawlDocumentLocationFactory;
import com.github.loa.source.service.DocumentLocationFactory;
import com.github.loa.url.service.URLConverter;
import io.micrometer.core.instrument.Counter;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.List;
import java.util.stream.Collectors;

@Configuration
@RequiredArgsConstructor
public class CommonCrawlConfiguration {

    @Qualifier("processedDocumentLocationCount")
    private final Counter processedDocumentLocationCount;

    @Bean
    public WebClient webClient() {
        return WebClient.create();
    }

    @Bean
    @ConditionalOnProperty(name = "loa.source.type", havingValue = "common-crawl")
    public DocumentLocationFactory warcDocumentLocationFactory(final WarcDownloader warcDownloader,
            final WarcRecordParser warcRecordParser, final WarcPathFactory warcPathFactory,
            final WarcFluxFactory warcFluxFactory, final URLConverter urlConverter,
            final CommonCrawlDocumentSourceConfigurationProperties commonCrawlDocumentSourceConfigurationProperties) {
        final List<String> paths =
                warcPathFactory.newPaths(commonCrawlDocumentSourceConfigurationProperties.getCrawlId()).stream()
                        .skip(commonCrawlDocumentSourceConfigurationProperties.getWarcId())
                        .collect(Collectors.toList());

        return new CommonCrawlDocumentLocationFactory(warcDownloader, warcRecordParser, warcFluxFactory,
                urlConverter, paths, processedDocumentLocationCount);
    }
}
