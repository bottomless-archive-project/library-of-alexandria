package com.github.bottomlessarchive.loa.io.service.downloader;

import com.github.bottomlessarchive.loa.url.service.downloader.DocumentLocationResultCalculator;
import com.github.bottomlessarchive.loa.url.service.downloader.domain.DownloadResult;
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
    private final DocumentLocationResultCalculator documentLocationResultCalculator;

    /**
     * Download a file from the provided url to the provided file location.
     *
     * @param downloadTarget the url to download the file from
     * @param resultLocation the location to download the file to
     */
    public DownloadResult downloadFile(final String downloadTarget, final Path resultLocation) {
        final Request request = new Request.Builder()
                .get()
                .url(downloadTarget)
                .build();

        DownloadResult documentLocationResultType;

        //TODO: Re-add the retry logic when the response is HttpStatus.TOO_MANY_REQUESTS
        try (Response response = downloaderClient.newCall(request).execute()) {
            try (ResponseBody responseBody = response.body()) {
                documentLocationResultType = documentLocationResultCalculator.calculateResultBasedOnResponseCode(response.code());

                if (documentLocationResultType == DownloadResult.UNKNOWN) {
                    log.info("Unknown response code: {} on location: {}.", response.code(), downloadTarget);
                }

                if (responseBody == null) {
                    return documentLocationResultType == DownloadResult.OK ? DownloadResult.EMPTY_BODY : documentLocationResultType;
                }

                Files.copy(responseBody.byteStream(), resultLocation);
            }
        } catch (final IOException e) {
            documentLocationResultType = documentLocationResultCalculator.transformExceptionToDownloadResult(e);

            try {
                if (Files.exists(resultLocation)) {
                    Files.delete(resultLocation);
                }
            } catch (final IOException e2) {
                log.error("Failed to delete file at the staging location!", e2);
            }
        }

        return documentLocationResultType;
    }
}
