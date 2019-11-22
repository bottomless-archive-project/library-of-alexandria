package com.github.loa.downloader.service.file;

import com.github.loa.url.service.FileDownloader;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.io.File;
import java.net.URL;
import java.nio.file.Path;

/**
 * 0this device is responsible to acquire a file from the an {@link URL}.
 */
@Service
@RequiredArgsConstructor
public class FileCollector {

    private final FileDownloader fileDownloader;

    public Mono<Path> acquireFile(final URL fileLocation, final Path resultLocation) {
        final String protocol = fileLocation.getProtocol();

        if ("http".equals(protocol) || "https".equals(protocol)) {
            return fileDownloader.downloadFile(fileLocation, resultLocation);
        }

        return Mono.empty();
    }
}
