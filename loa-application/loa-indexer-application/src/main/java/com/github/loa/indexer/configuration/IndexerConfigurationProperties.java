package com.github.loa.indexer.configuration;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.ConstructorBinding;

@Getter
@ConstructorBinding
@RequiredArgsConstructor
@ConfigurationProperties("loa.indexer")
public class IndexerConfigurationProperties {

    private final int maximumFileSize;
    private final int concurrentIndexerThreads;
}
