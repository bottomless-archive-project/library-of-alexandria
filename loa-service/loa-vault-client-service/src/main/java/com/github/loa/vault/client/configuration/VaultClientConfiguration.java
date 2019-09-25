package com.github.loa.vault.client.configuration;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
@RequiredArgsConstructor
public class VaultClientConfiguration {

    private final VaultClientConfigurationProperties vaultClientConfigurationProperties;

    @Bean
    public WebClient vaultWebClient() {
        return WebClient.builder()
                .baseUrl("http://" + vaultClientConfigurationProperties.getHost() + ":"
                        + vaultClientConfigurationProperties.getPort())
                .build();
    }
}
