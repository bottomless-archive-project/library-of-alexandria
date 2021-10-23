package com.github.bottomlessarchive.loa.vault.client.configuration;

import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.rsocket.RSocketRequester;
import org.springframework.messaging.rsocket.RSocketStrategies;

import java.util.Map;
import java.util.stream.Collectors;

@Configuration
@RequiredArgsConstructor
public class VaultClientConfiguration {

    private final VaultClientConfigurationProperties vaultClientConfigurationProperties;

    @Bean
    public Map<String, RSocketRequester> rSocketRequester(final RSocketStrategies rSocketStrategies) {
        return vaultClientConfigurationProperties.getLocations().keySet().stream()
                .map(vaultName -> {
                    final VaultClientLocationConfigurationProperties vaultClientLocationConfigurationProperties =
                            vaultClientConfigurationProperties.getLocation(vaultName);

                    return ImmutablePair.of(vaultName,
                            RSocketRequester.builder()
                                    .rsocketStrategies(rSocketStrategies)
                                    .tcp(vaultClientLocationConfigurationProperties.getHost(),
                                            vaultClientLocationConfigurationProperties.getPort())
                    );
                })
                .collect(Collectors.toMap(ImmutablePair::getLeft, ImmutablePair::getRight));
    }
}
