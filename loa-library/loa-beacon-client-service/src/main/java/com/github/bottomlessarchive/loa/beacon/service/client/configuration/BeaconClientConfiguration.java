package com.github.bottomlessarchive.loa.beacon.service.client.configuration;

import okhttp3.OkHttpClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Duration;

@Configuration
public class BeaconClientConfiguration {

    @Bean
    public OkHttpClient beaconWebClient() {
        return new OkHttpClient.Builder()
                .connectTimeout(Duration.ofSeconds(30))
                .callTimeout(Duration.ofMinutes(30))
                .readTimeout(Duration.ofMinutes(30))
                .writeTimeout(Duration.ofMinutes(30))
                .build();
    }
}
