package com.github.loa.source.commoncrawl.configuration;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * Contains the configurations that could be set from the property files regarding the common crawl based document location
 * source processing.
 */
@Data
@Component
@ConfigurationProperties("loa.source.commoncrawl")
public class CommonCrawlDocumentSourceConfigurationProperties {

    /**
     * The Id of the crawl. For example CC-MAIN-2019-09.
     */
    private String crawlId;

    /**
     * The Id of the WARC file in the crawl. Usually between 1 - 64000.
     */
    private int warcId;

    /**
     * How many schedulers should be available minimally to parse the document locations.
     */
    private int minimumWebpageProcessors;

    /**
     * How many schedulers should be available maximally to parse the document locations.
     */
    private int maximumWebpageProcessors;
}
