package com.github.bottomlessarchive.loa.vault.configuration;

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
public class VaultParallelismConfiguration {

    private final VaultConfigurationProperties vaultConfigurationProperties;

    @Bean
    public Semaphore vaultSemaphore() {
        return new Semaphore(vaultConfigurationProperties.parallelism() * 3);
    }

    @Bean
    public ExecutorService vaultExecutorService() {
        log.info("Initializing the downloader with parallelism level of {}.",
                vaultConfigurationProperties.parallelism());

        return Executors.newFixedThreadPool(vaultConfigurationProperties.parallelism());
    }
}
