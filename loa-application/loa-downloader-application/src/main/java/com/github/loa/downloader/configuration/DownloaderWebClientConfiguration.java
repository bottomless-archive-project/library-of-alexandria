package com.github.loa.downloader.configuration;

import io.netty.channel.ChannelOption;
import io.netty.handler.timeout.ReadTimeoutHandler;
import io.netty.handler.timeout.WriteTimeoutHandler;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.reactive.ClientHttpConnector;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.netty.http.client.HttpClient;
import reactor.netty.tcp.TcpClient;

import java.util.concurrent.TimeUnit;

@Configuration
public class DownloaderWebClientConfiguration {

    private static final int DOWNLOADER_CLIENT_TIMEOUT = 30000;

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
    protected HttpClient downloaderHttpClient(
            @Qualifier("downloaderTcpClient") final TcpClient downloaderTcpClient) {
        return HttpClient.from(downloaderTcpClient)
                .followRedirect(true);
    }

    @Bean
    protected TcpClient downloaderTcpClient() {
        return TcpClient.create()
                .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, DOWNLOADER_CLIENT_TIMEOUT)
                .doOnConnected(connection -> connection
                        .addHandlerLast(new ReadTimeoutHandler(DOWNLOADER_CLIENT_TIMEOUT, TimeUnit.MILLISECONDS))
                        .addHandlerLast(new WriteTimeoutHandler(DOWNLOADER_CLIENT_TIMEOUT, TimeUnit.MILLISECONDS))
                );
    }
}
