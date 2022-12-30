package com.github.bottomlessarchive.loa.stage.service;

import com.github.bottomlessarchive.loa.conductor.service.client.extension.InstancePropertyExtensionProvider;
import com.github.bottomlessarchive.loa.conductor.service.client.extension.domain.InstanceExtensionContext;
import com.github.bottomlessarchive.loa.stage.configuration.StagingConfigurationProperties;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.springframework.stereotype.Component;

import java.nio.file.Files;

@Component
@RequiredArgsConstructor
public class StagingFreeSpaceInstancePropertyExtensionProvider implements InstancePropertyExtensionProvider {

    private final StagingConfigurationProperties stagingConfigurationProperties;

    @Override
    @SneakyThrows
    public void extendInstanceWithProperty(final InstanceExtensionContext instanceExtensionContext) {
        instanceExtensionContext.setProperty("freeSpace", Files.getFileStore(stagingConfigurationProperties.location()).getUsableSpace());
    }
}
