package com.github.bottomlessarchive.loa.vault.service;

import com.github.bottomlessarchive.loa.conductor.service.client.extension.InstanceRegistrationExtensionProvider;
import com.github.bottomlessarchive.loa.conductor.service.client.extension.domain.InstanceRegistrationContext;
import com.github.bottomlessarchive.loa.vault.configuration.VaultConfigurationProperties;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class VaultNameInstanceRegistrationExtensionProvider implements InstanceRegistrationExtensionProvider {

    private final VaultConfigurationProperties vaultConfigurationProperties;

    @Override
    public void extendRegistration(final InstanceRegistrationContext instanceRegistrationContext) {
        instanceRegistrationContext.setProperty("name", vaultConfigurationProperties.getName());
    }
}
