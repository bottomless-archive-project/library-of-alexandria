package com.github.bottomlessarchive.loa.vault.client.configuration;

import com.github.bottomlessarchive.loa.application.domain.ApplicationType;
import com.github.bottomlessarchive.loa.conductor.service.client.ConductorClient;
import com.github.bottomlessarchive.loa.conductor.service.domain.ServiceInstanceEntity;
import com.github.bottomlessarchive.loa.conductor.service.domain.ServiceInstanceEntityProperty;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.rsocket.RSocketRequester;
import org.springframework.messaging.rsocket.RSocketStrategies;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Configuration
@RequiredArgsConstructor
public class VaultClientConfiguration {

    private final ConductorClient conductorClient;

    @Bean
    public Map<String, RSocketRequester> rSocketRequester(final RSocketStrategies rSocketStrategies) throws InterruptedException {
        List<ServiceInstanceEntity> serviceInstanceEntities = null;

        while (serviceInstanceEntities == null) {
            final List<ServiceInstanceEntity> serviceInstanceEntitiesResponse = conductorClient.getInstances(
                            ApplicationType.VAULT_APPLICATION)
                    .collect(Collectors.toList())
                    .block();

            if (serviceInstanceEntitiesResponse == null || serviceInstanceEntitiesResponse.isEmpty()) {
                log.info("Failed to acquire connection to a Vault Application. Will retry in 5 seconds.");

                Thread.sleep(5000L);
            } else {
                serviceInstanceEntities = serviceInstanceEntitiesResponse;
            }
        }

        return serviceInstanceEntities.stream()
                .map(serviceInstanceEntity -> {
                            final ServiceInstanceEntityProperty nameProperty = serviceInstanceEntity.getProperty("name")
                                    .orElseThrow(() -> new IllegalStateException("Can't find the name property for one of the vaults at " +
                                            "location: " + serviceInstanceEntity.getLocation() + " and port: "
                                            + serviceInstanceEntity.getPort() + " and id: " + serviceInstanceEntity.getId()));

                            log.info("Connecting to Vault Application - {} at location: {}, port: {}.", nameProperty.getName(),
                                    serviceInstanceEntity.getLocation(), serviceInstanceEntity.getPort());

                            return ImmutablePair.of(
                                    nameProperty.getValue(),
                                    RSocketRequester.builder()
                                            .rsocketStrategies(rSocketStrategies)
                                            .tcp(serviceInstanceEntity.getLocation(), serviceInstanceEntity.getPort())
                            );
                        }
                )
                .collect(Collectors.toMap(ImmutablePair::getLeft, ImmutablePair::getRight));
    }
}
