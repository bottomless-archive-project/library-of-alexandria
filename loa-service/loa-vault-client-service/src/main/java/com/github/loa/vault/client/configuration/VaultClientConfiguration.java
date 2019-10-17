package com.github.loa.vault.client.configuration;

import io.netty.channel.ChannelOption;
import io.netty.handler.timeout.ReadTimeoutHandler;
import io.netty.handler.timeout.WriteTimeoutHandler;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.netty.http.client.HttpClient;
import reactor.netty.tcp.TcpClient;

@Configuration
@RequiredArgsConstructor
public class VaultClientConfiguration {

    private final VaultClientConfigurationProperties vaultClientConfigurationProperties;

    @Bean
    public WebClient vaultWebClient(final TcpClient vaultTcpClient) {
        return WebClient.builder()
                .baseUrl("http://" + vaultClientConfigurationProperties.getHost() + ":"
                        + vaultClientConfigurationProperties.getPort())
                .clientConnector(new ReactorClientHttpConnector(
                        HttpClient.from(vaultTcpClient)
                                .compress(true)
                ))
                .build();
    }

    @Bean
    protected TcpClient vaultTcpClient() {
        return TcpClient.create()
                .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 60000)
                .doOnConnected(c -> c.addHandlerLast(new ReadTimeoutHandler(60))
                        .addHandlerLast(new WriteTimeoutHandler(60)));
    }
}
