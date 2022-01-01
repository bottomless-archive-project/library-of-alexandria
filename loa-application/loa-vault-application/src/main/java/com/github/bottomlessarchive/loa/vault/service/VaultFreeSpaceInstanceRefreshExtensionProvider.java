package com.github.bottomlessarchive.loa.vault.service;

import com.github.bottomlessarchive.loa.conductor.service.client.extension.InstanceRefreshExtensionProvider;
import com.github.bottomlessarchive.loa.conductor.service.client.extension.domain.InstanceRefreshContext;
import com.github.bottomlessarchive.loa.vault.service.location.VaultLocationFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class VaultFreeSpaceInstanceRefreshExtensionProvider implements InstanceRefreshExtensionProvider {

    private final VaultLocationFactory vaultLocationFactory;

    @Override
    public void extendRegistration(final InstanceRefreshContext instanceRefreshContext) {
        instanceRefreshContext.setProperty("freeSpace", String.valueOf(vaultLocationFactory.getAvailableSpace()));
    }
}
