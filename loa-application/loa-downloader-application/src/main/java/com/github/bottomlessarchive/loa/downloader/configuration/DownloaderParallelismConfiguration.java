package com.github.bottomlessarchive.loa.downloader.configuration;

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
public class DownloaderParallelismConfiguration {

    private final DownloaderConfigurationProperties downloaderConfigurationProperties;

    @Bean
    public Semaphore downloaderSemaphore() {
        return new Semaphore(downloaderConfigurationProperties.parallelism() * 3);
    }

    @Bean
    public ExecutorService downloaderExecutorService() {
        log.info("Initializing the downloader with parallelism level of {}.",
                downloaderConfigurationProperties.parallelism());

        return Executors.newFixedThreadPool(downloaderConfigurationProperties.parallelism());
    }
}
