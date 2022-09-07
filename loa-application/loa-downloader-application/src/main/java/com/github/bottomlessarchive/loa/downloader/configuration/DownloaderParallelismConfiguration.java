package com.github.bottomlessarchive.loa.downloader.configuration;

import com.github.bottomlessarchive.loa.threading.util.BlockingExecutor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Slf4j
@Configuration
@RequiredArgsConstructor
public class DownloaderParallelismConfiguration {

    private final DownloaderConfigurationProperties downloaderConfigurationProperties;

    @Bean
    public BlockingExecutor downloaderExecutorService() {
        log.info("Initializing the downloader with parallelism level of {}.",
                downloaderConfigurationProperties.parallelism());

        return new BlockingExecutor(downloaderConfigurationProperties.parallelism(),
                downloaderConfigurationProperties.parallelism() * 3);
    }
}
