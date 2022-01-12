package com.github.bottomlessarchive.loa.conductor.service.client.configuration;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
@EnableScheduling
public class ConductorClientConfiguration {

    @Bean
    public WebClient conductorWebClient() {
        return WebClient.builder()
                .build();
    }
}
