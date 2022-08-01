package com.github.bottomlessarchive.loa.url.service.downloader;

import com.github.bottomlessarchive.loa.location.service.DocumentLocationManipulator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.ResponseBody;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * This service is responsible for downloading files from the internet.
 */
@Slf4j
@Service
@RequiredArgsConstructor
@ConditionalOnBean(name = "downloaderClient")
public class FileDownloadManager {

    @Qualifier("downloaderClient")
    private final OkHttpClient downloaderClient;
    private final DocumentLocationManipulator documentLocationManipulator;

    /**
     * Download a file from the provided url to the provided file location.
     *
     * @param downloadTarget the url to download the file from
     * @param resultLocation the location to download the file to
     */
    public void downloadFile(final String documentLocationId, final URL downloadTarget, final Path resultLocation) {
        final Request request = new Request.Builder()
                .get()
                .url(downloadTarget)
                .build();

        //TODO: Re-add the retry logic when the response is HttpStatus.TOO_MANY_REQUESTS
        try (Response response = downloaderClient.newCall(request).execute()) {
            //TODO: We shouldn't directly save the response code but smthing else!
            documentLocationManipulator.updateDownloadStatus(documentLocationId, response.code());

            try (ResponseBody responseBody = response.body()) {
                if (responseBody == null) {
                    return;
                }

                Files.copy(responseBody.byteStream(), resultLocation);
            }
        } catch (final IOException e) {
            //TODO: Figure out how could we handle the IO exception
            try {
                if (Files.exists(resultLocation)) {
                    Files.delete(resultLocation);
                }
            } catch (final IOException e2) {
                log.error("Failed to delete file at the staging location!", e2);
            }
        }
    }
}
