package com.github.bottomlessarchive.loa.vault.service;

import com.github.bottomlessarchive.loa.conductor.service.client.extension.InstancePropertyExtensionProvider;
import com.github.bottomlessarchive.loa.conductor.service.client.extension.domain.InstanceExtensionContext;
import com.github.bottomlessarchive.loa.vault.configuration.VaultConfigurationProperties;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class VaultNameInstanceRegistrationExtensionProvider implements InstancePropertyExtensionProvider {

    private final VaultConfigurationProperties vaultConfigurationProperties;

    @Override
    public void extendInstanceWithProperty(final InstanceExtensionContext instanceExtensionContext) {
        instanceExtensionContext.setProperty("name", vaultConfigurationProperties.getName());
    }
}
