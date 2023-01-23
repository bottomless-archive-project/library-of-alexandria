package com.github.bottomlessarchive.loa.staging.service.client.configuration;

import okhttp3.OkHttpClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Duration;

@Configuration
public class StagingClientConfiguration {

    @Bean
    public OkHttpClient stagingWebClient() {
        return new OkHttpClient.Builder()
                .callTimeout(Duration.ofSeconds(30))
                .connectTimeout(Duration.ofSeconds(30))
                .readTimeout(Duration.ofSeconds(30))
                .writeTimeout(Duration.ofSeconds(30))
                .build();
    }
}
