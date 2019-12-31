package com.github.loa.url.service.downloader;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.core.io.buffer.DataBufferUtils;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.net.URL;
import java.nio.file.Path;

/**
 * This service is responsible for downloading files from the internet.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class FileDownloadManager {

    private final FileDownloadRequestFactory fileDownloadRequestFactory;

    /**
     * Download a file from the provided url to the provided file location.
     *
     * @param downloadTarget the url to download the file from
     * @param resultLocation the location to download the file to
     */
    public Mono<Path> downloadFile(final URL downloadTarget, final Path resultLocation) {
        try {
            final Flux<DataBuffer> dataBufferFlux =
                    fileDownloadRequestFactory.newDownloadRequest(downloadTarget.toURI());

            return DataBufferUtils.write(dataBufferFlux, resultLocation)
                    .doOnError(error -> resultLocation.toFile().delete())
                    .thenReturn(resultLocation);
        } catch (final Exception e) {
            log.debug("Failed to download document from location: {}.", downloadTarget, e);

            return Mono.empty();
        }
    }
}
