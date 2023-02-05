package com.github.bottomlessarchive.loa.stage.service;

import com.github.bottomlessarchive.loa.conductor.service.client.extension.InstancePropertyExtensionProvider;
import com.github.bottomlessarchive.loa.conductor.service.client.extension.domain.InstanceExtensionContext;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class StagingFreeSpaceInstancePropertyExtensionProvider implements InstancePropertyExtensionProvider {

    private final StageLocationFactory stageLocationFactory;

    @Override
    @SneakyThrows
    public void extendInstanceWithProperty(final InstanceExtensionContext instanceExtensionContext) {
        instanceExtensionContext.setProperty("freeSpace", stageLocationFactory.getAvailableSpace());
    }
}
