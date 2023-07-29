package com.github.bottomlessarchive.loa.url.service.downloader;

import com.github.bottomlessarchive.loa.url.service.downloader.domain.DownloadResult;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.net.NoRouteToHostException;
import java.net.ProtocolException;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;

@Slf4j
@Service
@RequiredArgsConstructor
public class DocumentLocationResultCalculator {

    public DownloadResult calculateResultBasedOnResponseCode(final int responseCode) {
        return switch (responseCode) {
            case 200 -> DownloadResult.OK;
            case 404 -> DownloadResult.NOT_FOUND;
            case 403 -> DownloadResult.FORBIDDEN;
            case 400 -> DownloadResult.SERVER_ERROR;
            default -> DownloadResult.UNKNOWN;
        };
    }

    public DownloadResult transformExceptionToDownloadResult(final Exception e) {
        if (e instanceof UnknownHostException || e instanceof NoRouteToHostException) {
            return DownloadResult.ORIGIN_NOT_FOUND;
        } else if (e instanceof SocketTimeoutException) {
            return DownloadResult.TIMEOUT;
        } else if (e instanceof ProtocolException && e.getMessage().contains("unexpected end of stream")) {
            return DownloadResult.CONNECTION_ERROR;
        }

        return DownloadResult.UNKNOWN;
    }
}
