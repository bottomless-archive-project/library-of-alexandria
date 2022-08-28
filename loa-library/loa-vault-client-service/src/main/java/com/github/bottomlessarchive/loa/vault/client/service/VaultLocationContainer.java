package com.github.bottomlessarchive.loa.vault.client.service;

import com.github.bottomlessarchive.loa.vault.client.service.domain.VaultLocation;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
public class VaultLocationContainer {

    private final VaultLocationFetcher vaultLocationFetcher;

    private Map<String, VaultLocation> vaultLocations = Collections.emptyMap();

    @Scheduled(fixedRate = 1, timeUnit = TimeUnit.MINUTES)
    public void updateVaultLocations() {
        vaultLocations = vaultLocationFetcher.fetchVaultLocations();
    }

    public Optional<VaultLocation> getVaultLocation(final String vaultName) {
        return Optional.ofNullable(vaultLocations.get(vaultName));
    }
}
