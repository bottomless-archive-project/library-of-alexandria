package com.github.loa.indexer.configuration;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties("loa.indexer")
public class IndexerConfigurationProperties {

    private int sleepTime;
    private int maximumFileSize;
    private int concurrentIndexerThreads;
}
