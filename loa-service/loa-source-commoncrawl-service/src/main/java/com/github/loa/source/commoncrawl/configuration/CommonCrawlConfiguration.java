package com.github.loa.source.commoncrawl.configuration;

import com.github.bottomlessarchive.commoncrawl.WarcLocationFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import reactor.core.scheduler.Scheduler;
import reactor.core.scheduler.Schedulers;

import java.net.URL;
import java.util.List;
import java.util.stream.Collectors;

@Configuration
@RequiredArgsConstructor
@ConditionalOnProperty(name = "loa.source.type", havingValue = "common-crawl")
public class CommonCrawlConfiguration {

    private final CommonCrawlDocumentSourceConfigurationProperties commonCrawlDocumentSourceConfigurationProperties;

    @Bean
    public List<URL> warcLocations(final WarcLocationFactory warcLocationFactory) {
        return warcLocationFactory
                .newUrls(commonCrawlDocumentSourceConfigurationProperties.getCrawlId()).stream()
                .skip(commonCrawlDocumentSourceConfigurationProperties.getWarcId())
                .collect(Collectors.toList());
    }

    @Bean
    public Scheduler documentLocationParserScheduler() {
        return Schedulers.newBoundedElastic(
                commonCrawlDocumentSourceConfigurationProperties.getMinimumRecordProcessors(),
                commonCrawlDocumentSourceConfigurationProperties.getMaximumRecordProcessors(),
                "parsing-scheduler"
        );
    }

    @Bean
    public WarcLocationFactory warcLocationFactory() {
        return new WarcLocationFactory();
    }
}
