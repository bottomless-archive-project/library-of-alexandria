package com.github.bottomlessarchive.loa.downloader.configuration;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.ConstructorBinding;

@Getter
@ConstructorBinding
@RequiredArgsConstructor
@ConfigurationProperties("loa.downloader")
public class DownloaderConfigurationProperties {

    /**
     * 1 = M1 - RC-2 -> Updated because normal files had a dot at the end.
     * 2 = RC-3+
     */
    private final int versionNumber;

    private final SourceLocation source;
}
