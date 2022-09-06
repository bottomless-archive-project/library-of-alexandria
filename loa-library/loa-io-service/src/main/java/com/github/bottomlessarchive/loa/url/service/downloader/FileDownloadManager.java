package com.github.bottomlessarchive.loa.url.service.downloader;

import com.github.bottomlessarchive.loa.location.domain.DocumentLocationResultType;
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
import java.net.NoRouteToHostException;
import java.net.ProtocolException;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.net.UnknownHostException;
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
    private final DownloadResultReporter downloadResultReporter;

    /**
     * Download a file from the provided url to the provided file location.
     *
     * @param downloadTarget the url to download the file from
     * @param resultLocation the location to download the file to
     */
    public DocumentLocationResultType downloadFile(final URL downloadTarget, final Path resultLocation) {
        final Request request = new Request.Builder()
                .get()
                .url(downloadTarget)
                .build();

        DocumentLocationResultType documentLocationResultType = DocumentLocationResultType.UNKNOWN;

        //TODO: Re-add the retry logic when the response is HttpStatus.TOO_MANY_REQUESTS
        try (Response response = downloaderClient.newCall(request).execute()) {
            try (ResponseBody responseBody = response.body()) {
                documentLocationResultType = downloadResultReporter.calculateResultBasedOnResponseCode(downloadTarget, response.code());

                if (responseBody == null) {
                    return documentLocationResultType == DocumentLocationResultType.OK
                            ? DocumentLocationResultType.EMPTY_BODY : documentLocationResultType;
                }

                Files.copy(responseBody.byteStream(), resultLocation);
            }
        } catch (final IOException e) {
            if (e instanceof UnknownHostException || e instanceof NoRouteToHostException) {
                documentLocationResultType = DocumentLocationResultType.ORIGIN_NOT_FOUND;
            } else if (e instanceof SocketTimeoutException) {
                documentLocationResultType = DocumentLocationResultType.TIMEOUT;
            } else if (e instanceof ProtocolException && e.getMessage().contains("unexpected end of stream")) {
                documentLocationResultType = DocumentLocationResultType.CONNECTION_ERROR;
            } else {
                log.error("Error while downloading document form {}.", downloadTarget, e);
            }

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
