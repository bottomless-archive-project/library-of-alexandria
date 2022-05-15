package com.github.bottomlessarchive.loa.indexer.service.conductor;

import com.github.bottomlessarchive.loa.conductor.service.client.extension.InstancePropertyExtensionProvider;
import com.github.bottomlessarchive.loa.conductor.service.client.extension.domain.InstanceExtensionContext;
import com.github.bottomlessarchive.loa.indexer.configuration.IndexerConfigurationProperties;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ParallelismInstancePropertyExtensionProvider implements InstancePropertyExtensionProvider {

    private final IndexerConfigurationProperties indexerConfigurationProperties;

    @Override
    public void extendInstanceWithProperty(final InstanceExtensionContext instanceExtensionContext) {
        instanceExtensionContext.setProperty("parallelism", indexerConfigurationProperties.parallelism());
        instanceExtensionContext.setProperty("batchSize", indexerConfigurationProperties.batchSize());
    }
}
