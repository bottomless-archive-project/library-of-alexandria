package com.github.loa.source.commoncrawl.configuration;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.ConstructorBinding;

/**
 * Contains the configurations that could be set from the property files regarding the common crawl based document location
 * source processing.
 */
@Getter
@ConstructorBinding
@RequiredArgsConstructor
@ConfigurationProperties("loa.source.commoncrawl")
public class CommonCrawlDocumentSourceConfigurationProperties {

    /**
     * The id of the crawl. For example CC-MAIN-2019-09.
     */
    private final String crawlId;

    /**
     * The id of the WARC file in the crawl. Usually between 1 - 64000.
     */
    private final int warcId;

    /**
     * How many schedulers should be available maximally to parse the document locations.
     */
    private final int maximumRecordProcessors;
}
