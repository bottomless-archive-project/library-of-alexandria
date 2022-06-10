package com.github.bottomlessarchive.loa.vault.client.service;

import com.github.bottomlessarchive.loa.application.domain.ApplicationType;
import com.github.bottomlessarchive.loa.conductor.service.client.ConductorClient;
import com.github.bottomlessarchive.loa.conductor.service.domain.ServiceInstanceEntityProperty;
import com.github.bottomlessarchive.loa.vault.client.service.domain.VaultLocation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class VaultLocationFetcher {

    private final ConductorClient conductorClient;

    public Map<String, VaultLocation> fetchVaultLocations() {
        return conductorClient.getInstances(ApplicationType.VAULT_APPLICATION).stream()
                .map(serviceInstanceEntity -> {
                            final ServiceInstanceEntityProperty nameProperty = serviceInstanceEntity.getProperty("name")
                                    .orElseThrow(() -> new IllegalStateException("Can't find the name property for one of the vaults at "
                                            + "location: " + serviceInstanceEntity.getLocation() + " and port: "
                                            + serviceInstanceEntity.getPort() + " and id: " + serviceInstanceEntity.getId()));

                            log.info("Connecting to Vault Application - {} at location: {}, port: {}.", nameProperty.getName(),
                                    serviceInstanceEntity.getLocation(), serviceInstanceEntity.getPort());

                            return ImmutablePair.of(
                                    nameProperty.getValue(),
                                    VaultLocation.builder()
                                            .location(serviceInstanceEntity.getLocation())
                                            .port(serviceInstanceEntity.getPort())
                                            .build()
                            );
                        }
                )
                .collect(Collectors.toMap(ImmutablePair::getLeft, ImmutablePair::getRight));
    }
}
