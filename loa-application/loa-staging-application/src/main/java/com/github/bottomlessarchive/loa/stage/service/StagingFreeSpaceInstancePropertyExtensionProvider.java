package com.github.bottomlessarchive.loa.stage.service;

import com.github.bottomlessarchive.loa.conductor.service.client.extension.InstancePropertyExtensionProvider;
import com.github.bottomlessarchive.loa.conductor.service.client.extension.domain.InstanceExtensionContext;
import com.github.bottomlessarchive.loa.stage.configuration.StagingConfigurationProperties;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.io.File;

@Component
@RequiredArgsConstructor
public class StagingFreeSpaceInstancePropertyExtensionProvider implements InstancePropertyExtensionProvider {

    private final StagingConfigurationProperties stagingConfigurationProperties;

    @Override
    public void extendInstanceWithProperty(final InstanceExtensionContext instanceExtensionContext) {
        instanceExtensionContext.setProperty("freeSpace", String.valueOf(
                new File(stagingConfigurationProperties.location()).getUsableSpace()));
    }
}
