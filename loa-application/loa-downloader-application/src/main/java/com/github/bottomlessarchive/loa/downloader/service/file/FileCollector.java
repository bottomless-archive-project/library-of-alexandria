package com.github.bottomlessarchive.loa.downloader.service.file;

import com.github.bottomlessarchive.loa.url.service.downloader.FileDownloadManager;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.springframework.stereotype.Service;

import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * This service is responsible to acquire a file from an {@link URL}.
 */
@Service
@RequiredArgsConstructor
public class FileCollector {

    private final FileDownloadManager fileDownloadManager;

    public void acquireFile(final URL fileLocation, final Path resultLocation) {
        final String protocol = fileLocation.getProtocol();

        if ("http".equals(protocol) || "https".equals(protocol)) {
            fileDownloadManager.downloadFile(fileLocation, resultLocation);
        } else if ("file".equals(protocol)) {
            copyFile(fileLocation, resultLocation);
        }
    }

    @SneakyThrows
    private void copyFile(final URL fileLocation, final Path resultLocation) {
        Files.copy(Path.of(fileLocation.toURI()), resultLocation);
    }
}
