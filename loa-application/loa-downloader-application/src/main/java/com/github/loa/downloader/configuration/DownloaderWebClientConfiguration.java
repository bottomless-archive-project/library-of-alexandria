package com.github.loa.downloader.configuration;

import io.netty.channel.ChannelOption;
import io.netty.handler.timeout.ReadTimeoutHandler;
import io.netty.handler.timeout.WriteTimeoutHandler;
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
    public WebClient downloaderWebClient(final ClientHttpConnector clientHttpConnector) {
        return WebClient.builder()
                .clientConnector(clientHttpConnector)
                .build();
    }

    @Bean
    protected ClientHttpConnector clientHttpConnector(final HttpClient httpClient) {
        return new ReactorClientHttpConnector(httpClient);
    }

    @Bean
    protected HttpClient httpClient(final TcpClient tcpClient) {
        return HttpClient.from(tcpClient)
                .followRedirect(true);
    }

    @Bean
    protected TcpClient tcpClient() {
        return TcpClient.create()
                .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, DOWNLOADER_CLIENT_TIMEOUT)
                .doOnConnected(connection -> connection
                        .addHandlerLast(new ReadTimeoutHandler(DOWNLOADER_CLIENT_TIMEOUT, TimeUnit.MILLISECONDS))
                        .addHandlerLast(new WriteTimeoutHandler(DOWNLOADER_CLIENT_TIMEOUT, TimeUnit.MILLISECONDS))
                );
    }
}
