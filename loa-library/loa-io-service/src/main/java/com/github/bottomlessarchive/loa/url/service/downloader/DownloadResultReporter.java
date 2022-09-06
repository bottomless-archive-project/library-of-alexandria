package com.github.bottomlessarchive.loa.url.service.downloader;

import com.github.bottomlessarchive.loa.location.domain.DocumentLocationResultType;
import com.github.bottomlessarchive.loa.location.service.DocumentLocationManipulator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.net.URL;

@Slf4j
@Service
@RequiredArgsConstructor
public class DownloadResultReporter {

    private final DocumentLocationManipulator documentLocationManipulator;

    public void updateResultToEmptyBody(final String documentLocationId) {
        documentLocationManipulator.updateDownloadResultCode(documentLocationId, DocumentLocationResultType.EMPTY_BODY);
    }

    public void updateResultToOriginNotFound(final String documentLocationId) {
        documentLocationManipulator.updateDownloadResultCode(documentLocationId, DocumentLocationResultType.ORIGIN_NOT_FOUND);
    }

    public void updateResultToTimeout(final String documentLocationId) {
        documentLocationManipulator.updateDownloadResultCode(documentLocationId, DocumentLocationResultType.TIMEOUT);
    }

    public void updateResultToConnectionError(final String documentLocationId) {
        documentLocationManipulator.updateDownloadResultCode(documentLocationId, DocumentLocationResultType.CONNECTION_ERROR);
    }

    public DocumentLocationResultType calculateResultBasedOnResponseCode(final URL downloadTarget, final int responseCode) {
        switch (responseCode) {
            case 200 -> {
                return DocumentLocationResultType.OK;
            }
            case 404 -> {
                return DocumentLocationResultType.NOT_FOUND;
            }
            case 403 -> {
                return DocumentLocationResultType.FORBIDDEN;
            }
            case 400 -> {
                return DocumentLocationResultType.SERVER_ERROR;
            }
            default -> {
                log.info("Unknown response code: {} on location: {}.", responseCode, downloadTarget);

                return DocumentLocationResultType.UNKNOWN;
            }
        }
    }
}
