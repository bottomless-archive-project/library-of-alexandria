package com.github.loa.downloader.configuration;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties("loa.downloader")
public class DownloaderConfigurationProperties {

    /**
     * 1 = M1 - RC-2 -> Updated because normal files had a dot at the end.
     * 2 = RC-3+
     */
    private int versionNumber;

    /**
     * Maximum file size to archive to the vault in bytes. Documents bigger than this in size are not archived.
     */
    private long maximumArchiveSize = 8589934592L;

    /**
     * The maximum amount of document locations that should be pre-fetched for processing. This parameter is important
     * when a high percentage of the documents are coming from the same host. When we limit the concurrent download
     * count for a domain, we create a URL queue. The size of that queue is set by this variable. If the queue size is
     * to small and the domains in the queue are the same then the application will use only one thread to download the
     * documents (and because of this slow down).
     */
    private int maximumPrefetchSize = 1000;

    /**
     * The delay in milliseconds that we should use when downloading items from the same domains.
     */
    private int sameDomainDownloadDelay = 1000;
}
