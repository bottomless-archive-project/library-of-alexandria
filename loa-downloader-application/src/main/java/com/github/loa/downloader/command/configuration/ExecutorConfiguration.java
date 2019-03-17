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

    private final DownloaderExecutorConfiguration downloaderExecutorConfiguration;

    @Bean
    public ExecutorService downloaderExecutor() {
        return Executors.newFixedThreadPool(downloaderExecutorConfiguration.getThreadCount());
    }

    @Bean
    public Semaphore downloaderSemaphore() {
        return new Semaphore(downloaderExecutorConfiguration.getQueueLength());
    }
}
