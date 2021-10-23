package com.github.bottomlessarchive.loa.downloader.configuration;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.reactive.ClientHttpConnector;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.netty.http.client.HttpClient;

import java.time.Duration;

@Configuration
public class DownloaderWebClientConfiguration {

    private static final int DOWNLOADER_CLIENT_TIMEOUT = 10;

    @Bean
    public WebClient downloaderWebClient(
            @Qualifier("downloaderClientHttpConnector") final ClientHttpConnector downloaderClientHttpConnector) {
        return WebClient.builder()
                .clientConnector(downloaderClientHttpConnector)
                .build();
    }

    @Bean
    protected ClientHttpConnector downloaderClientHttpConnector(
            @Qualifier("downloaderHttpClient") final HttpClient downloaderHttpClient) {
        return new ReactorClientHttpConnector(downloaderHttpClient);
    }

    @Bean
    protected HttpClient downloaderHttpClient() {
        return HttpClient.create()
                .responseTimeout(Duration.ofSeconds(DOWNLOADER_CLIENT_TIMEOUT))
                .followRedirect(true);
    }
}
