package com.github.loa.generator.command.batch.commoncrawl.configuration;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
public class WarcConfiguration {

    @Bean
    public WebClient webClient() {
        return WebClient.create();
    }
}
