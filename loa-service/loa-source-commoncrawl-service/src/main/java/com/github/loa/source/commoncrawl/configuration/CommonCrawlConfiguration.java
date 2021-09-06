package com.github.loa.source.commoncrawl.configuration;

import com.github.bottomlessarchive.commoncrawl.WarcLocationFactory;
import com.github.loa.source.commoncrawl.domain.CommonCrawlWarcLocation;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import reactor.core.scheduler.Scheduler;
import reactor.core.scheduler.Schedulers;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

@Configuration
@RequiredArgsConstructor
@ConditionalOnProperty(name = "loa.source.type", havingValue = "common-crawl")
public class CommonCrawlConfiguration {

    private final CommonCrawlDocumentSourceConfigurationProperties commonCrawlDocumentSourceConfigurationProperties;

    @Bean
    public List<CommonCrawlWarcLocation> warcLocations(final WarcLocationFactory warcLocationFactory) {
        final AtomicInteger atomicInteger = new AtomicInteger();

        return warcLocationFactory
                .newUrls(commonCrawlDocumentSourceConfigurationProperties.getCrawlId()).stream()
                .map(url ->
                        CommonCrawlWarcLocation.builder()
                                .id(atomicInteger.getAndIncrement())
                                .location(url)
                                .build()
                )
                .skip(commonCrawlDocumentSourceConfigurationProperties.getWarcId())
                .toList();
    }

    @Bean
    public Scheduler documentLocationParserScheduler() {
        return Schedulers.newBoundedElastic(commonCrawlDocumentSourceConfigurationProperties.getMaximumRecordProcessors(),
                Integer.MAX_VALUE, "parsing-scheduler");
    }

    @Bean
    public WarcLocationFactory warcLocationFactory() {
        return new WarcLocationFactory();
    }
}
