package com.github.bottomlessarchive.loa.url.service.downloader;

import com.github.bottomlessarchive.loa.url.service.downloader.domain.DownloadResult;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.net.URL;

@Slf4j
@Service
@RequiredArgsConstructor
public class DocumentLocationResultCalculator {

    public DownloadResult calculateResultBasedOnResponseCode(final URL downloadTarget, final int responseCode) {
        switch (responseCode) {
            case 200 -> {
                return DownloadResult.OK;
            }
            case 404 -> {
                return DownloadResult.NOT_FOUND;
            }
            case 403 -> {
                return DownloadResult.FORBIDDEN;
            }
            case 400 -> {
                return DownloadResult.SERVER_ERROR;
            }
            default -> {
                log.info("Unknown response code: {} on location: {}.", responseCode, downloadTarget);

                return DownloadResult.UNKNOWN;
            }
        }
    }
}
