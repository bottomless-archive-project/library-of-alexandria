package com.github.bottomlessarchive.loa.vault.service;

import com.github.bottomlessarchive.loa.conductor.service.client.extension.InstancePropertyExtensionProvider;
import com.github.bottomlessarchive.loa.conductor.service.client.extension.domain.InstanceExtensionContext;
import com.github.bottomlessarchive.loa.vault.service.location.VaultLocationFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class VaultFreeSpaceInstancePropertyExtensionProvider implements InstancePropertyExtensionProvider {

    private final VaultLocationFactory vaultLocationFactory;

    @Override
    public void extendInstanceWithProperty(final InstanceExtensionContext instanceExtensionContext) {
        instanceExtensionContext.setProperty("freeSpace", String.valueOf(vaultLocationFactory.getAvailableSpace()));
    }
}
