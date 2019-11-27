package com.github.loa.downloader.configuration;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.reactive.ClientHttpConnector;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.netty.http.client.HttpClient;

@Configuration
public class DownloaderWebClientConfiguration {

    @Bean
    public WebClient downloaderWebClient(final ClientHttpConnector clientHttpConnector) {
        return WebClient.builder()
                .clientConnector(clientHttpConnector)
                .build();
    }

    @Bean
    protected ClientHttpConnector clientHttpConnector() {
        return new ReactorClientHttpConnector(
                HttpClient.create()
                        .followRedirect(true)
        );
    }
}
