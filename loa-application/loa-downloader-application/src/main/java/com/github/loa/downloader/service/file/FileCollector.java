package com.github.loa.downloader.service.file;

import com.github.loa.url.service.downloader.FileDownloadManager;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;
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
        } else if ("file".equals(protocol)) {
            try {
                Files.copy(Path.of(fileLocation.toURI()), resultLocation);

                return Mono.just(resultLocation);
            } catch (URISyntaxException | IOException e) {
                throw new RuntimeException(e);
            }
        }

        return Mono.empty();
    }
}
