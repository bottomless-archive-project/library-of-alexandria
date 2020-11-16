package com.github.loa.vault.client.configuration;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.reactive.ClientHttpConnector;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.web.reactive.function.client.ExchangeStrategies;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.netty.http.client.HttpClient;

import java.time.Duration;

@Configuration
@RequiredArgsConstructor
public class VaultClientConfiguration {

    private static final int VAULT_CLIENT_TIMEOUT = 120;

    @Bean
    public WebClient vaultWebClient(
            @Qualifier("vaultClientHttpConnector") final ClientHttpConnector vaultClientHttpConnector) {
        return WebClient.builder()
                .exchangeStrategies(
                        ExchangeStrategies.builder()
                                .codecs(configurer ->
                                        configurer.defaultCodecs()
                                                .maxInMemorySize(-1)
                                )
                                .build()
                )
                .clientConnector(vaultClientHttpConnector)
                .build();
    }

    @Bean
    protected ClientHttpConnector vaultClientHttpConnector(
            @Qualifier("vaultHttpClient") final HttpClient vaultHttpClient) {
        return new ReactorClientHttpConnector(vaultHttpClient);
    }

    @Bean
    protected HttpClient vaultHttpClient() {
        return HttpClient.create()
                .responseTimeout(Duration.ofSeconds(VAULT_CLIENT_TIMEOUT))
                .compress(true);
    }
}
