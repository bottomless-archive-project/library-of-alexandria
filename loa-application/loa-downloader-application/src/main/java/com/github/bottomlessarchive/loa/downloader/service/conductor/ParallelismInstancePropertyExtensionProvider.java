package com.github.bottomlessarchive.loa.downloader.service.conductor;

import com.github.bottomlessarchive.loa.conductor.service.client.extension.InstancePropertyExtensionProvider;
import com.github.bottomlessarchive.loa.conductor.service.client.extension.domain.InstanceExtensionContext;
import com.github.bottomlessarchive.loa.downloader.configuration.DownloaderConfigurationProperties;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ParallelismInstancePropertyExtensionProvider implements InstancePropertyExtensionProvider {

    private final DownloaderConfigurationProperties downloaderConfigurationProperties;

    @Override
    public void extendInstanceWithProperty(final InstanceExtensionContext instanceExtensionContext) {
        instanceExtensionContext.setProperty("parallelism", downloaderConfigurationProperties.parallelism());
    }
}
