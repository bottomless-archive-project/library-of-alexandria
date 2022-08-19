package com.github.bottomlessarchive.loa.downloader.configuration;

import lombok.RequiredArgsConstructor;
import okhttp3.ConnectionPool;
import okhttp3.Dispatcher;
import okhttp3.OkHttpClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.TimeUnit;

@Configuration
@RequiredArgsConstructor
public class DownloaderClientConfiguration {

    private final DownloaderConfigurationProperties downloaderConfigurationProperties;

    @Bean
    public OkHttpClient downloaderClient() {
        return new OkHttpClient.Builder()
                .connectionPool(connectionPool())
                .dispatcher(dispatcher())
                .build();
    }

    private Dispatcher dispatcher() {
        final Dispatcher dispatcher = new Dispatcher();

        dispatcher.setMaxRequests(downloaderConfigurationProperties.parallelism());
        dispatcher.setMaxRequestsPerHost(10);

        return dispatcher;
    }

    private ConnectionPool connectionPool() {
        return new ConnectionPool(downloaderConfigurationProperties.parallelism(), 5, TimeUnit.MINUTES);
    }
}
