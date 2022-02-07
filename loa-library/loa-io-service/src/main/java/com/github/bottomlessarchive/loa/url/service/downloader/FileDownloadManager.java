package com.github.bottomlessarchive.loa.url.service.downloader;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * This service is responsible for downloading files from the internet.
 */
@Slf4j
@Service
@RequiredArgsConstructor
@ConditionalOnBean(WebClient.class)
public class FileDownloadManager {

    private final OkHttpClient okHttpClient;

    /**
     * Download a file from the provided url to the provided file location.
     *
     * @param downloadTarget the url to download the file from
     * @param resultLocation the location to download the file to
     */
    public void downloadFile(final URL downloadTarget, final Path resultLocation) {
        final Request request = new Request.Builder()
                .get()
                .url(downloadTarget)
                .build();

        try {
            //TODO: Re-add the retry logic when the response is HttpStatus.TOO_MANY_REQUESTS
            final InputStream fileInputStream = okHttpClient.newCall(request)
                    .execute()
                    .body()
                    .byteStream();

            Files.copy(fileInputStream, resultLocation);
        } catch (final IOException e) {
            try {
                Files.delete(resultLocation);
            } catch (final IOException e2) {
                log.error("Failed to delete file at the staging location!", e2);
            }
        }
    }
}
