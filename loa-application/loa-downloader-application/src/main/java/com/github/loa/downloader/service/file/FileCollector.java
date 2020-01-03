package com.github.loa.downloader.service.file;

import com.github.loa.url.service.downloader.FileDownloadManager;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.net.URL;
import java.nio.file.Path;

/**
 * 0this device is responsible to acquire a file from the an {@link URL}.
 */
@Service
@RequiredArgsConstructor
public class FileCollector {

    private final FileDownloadManager fileDownloadManager;

    public Mono<Path> acquireFile(final URL fileLocation, final Path resultLocation) {
        final String protocol = fileLocation.getProtocol();

        if ("http".equals(protocol) || "https".equals(protocol)) {
            return fileDownloadManager.downloadFile(fileLocation, resultLocation);
        }

        return Mono.empty();
    }
}
