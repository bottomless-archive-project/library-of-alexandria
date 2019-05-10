package com.github.loa.downloader.command.configuration;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Semaphore;

@Configuration
@RequiredArgsConstructor
public class ExecutorConfiguration {

    private final DownloaderExecutorConfigurationProperties downloaderExecutorConfigurationProperties;

    @Bean
    public ExecutorService downloaderExecutor() {
        return Executors.newFixedThreadPool(downloaderExecutorConfigurationProperties.getThreadCount());
    }

    @Bean
    public Semaphore downloaderSemaphore() {
        return new Semaphore(downloaderExecutorConfigurationProperties.getQueueLength());
    }
}
