package com.github.bottomlessarchive.loa.administrator.service;

import com.github.bottomlessarchive.loa.administrator.configuration.AdministratorCommandConfigurationProperties;
import com.github.bottomlessarchive.loa.conductor.service.client.extension.InstancePropertyExtensionProvider;
import com.github.bottomlessarchive.loa.conductor.service.client.extension.domain.InstanceExtensionContext;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AdministratorInstancePropertyExtensionProvider implements InstancePropertyExtensionProvider {

    private final AdministratorCommandConfigurationProperties administratorCommandConfigurationProperties;

    @Override
    public void extendInstanceWithProperty(final InstanceExtensionContext instanceExtensionContext) {
        instanceExtensionContext.setProperty("command", administratorCommandConfigurationProperties.name());
    }
}
