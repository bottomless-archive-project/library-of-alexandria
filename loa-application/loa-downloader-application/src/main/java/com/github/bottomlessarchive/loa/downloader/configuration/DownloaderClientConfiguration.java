package com.github.bottomlessarchive.loa.downloader.configuration;

import okhttp3.OkHttpClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class DownloaderClientConfiguration {

    @Bean
    public OkHttpClient downloaderClient() {
        return new OkHttpClient.Builder()
                .build();
    }
}
