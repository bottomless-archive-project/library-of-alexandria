package com.github.bottomlessarchive.loa.conductor.service.client.configuration;

import okhttp3.OkHttpClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;

@Configuration
@EnableScheduling
public class ConductorClientConfiguration {

    @Bean
    public OkHttpClient conductorWebClient() {
        return new OkHttpClient.Builder()
                .build();
    }
}
