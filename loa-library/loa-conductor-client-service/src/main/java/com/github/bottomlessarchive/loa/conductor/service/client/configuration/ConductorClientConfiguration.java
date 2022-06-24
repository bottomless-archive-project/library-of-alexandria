package com.github.bottomlessarchive.loa.conductor.service.client.configuration;

import okhttp3.OkHttpClient;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;

import java.time.Duration;

@Configuration
@EnableCaching
@EnableScheduling
public class ConductorClientConfiguration {

    @Bean
    public OkHttpClient conductorWebClient() {
        return new OkHttpClient.Builder()
                .callTimeout(Duration.ofSeconds(30))
                .connectTimeout(Duration.ofSeconds(30))
                .readTimeout(Duration.ofSeconds(30))
                .writeTimeout(Duration.ofSeconds(30))
                .build();
    }
}
