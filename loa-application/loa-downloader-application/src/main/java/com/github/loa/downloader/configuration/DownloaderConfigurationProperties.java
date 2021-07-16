package com.github.loa.downloader.configuration;

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

    /**
     * Maximum file size to archive in bytes. Documents bigger than this in size are not archived.
     */
    private final long maximumArchiveSize = 8589934592L;

    private final SourceLocation source;
}
