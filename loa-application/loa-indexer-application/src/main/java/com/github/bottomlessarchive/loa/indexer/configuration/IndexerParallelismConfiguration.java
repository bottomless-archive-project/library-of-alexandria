package com.github.bottomlessarchive.loa.indexer.configuration;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Semaphore;

@Slf4j
@Configuration
@RequiredArgsConstructor
public class IndexerParallelismConfiguration {

    private final IndexerConfigurationProperties indexerConfigurationProperties;

    @Bean
    public Semaphore indexerSemaphore() {
        return new Semaphore(indexerConfigurationProperties.parallelism() * 3);
    }

    @Bean
    public ExecutorService indexerExecutorService() {
        log.info("Initializing the indexer with parallelism level of {}.",
                indexerConfigurationProperties.parallelism());

        return Executors.newFixedThreadPool(indexerConfigurationProperties.parallelism());
    }
}
