package com.github.bottomlessarchive.loa.web.view.location.service;

import com.github.bottomlessarchive.loa.location.service.factory.domain.DocumentLocation;
import com.github.bottomlessarchive.loa.web.view.location.response.DocumentLocationDebugResponse;
import org.springframework.stereotype.Service;

@Service
public class DocumentLocationDebugResponseFactory {

    public DocumentLocationDebugResponse newDocumentLocationDebugResponse(final DocumentLocation documentLocation) {
        return DocumentLocationDebugResponse.builder()
                .id(documentLocation.getId())
                .url(documentLocation.getUrl())
                .source(documentLocation.getSource())
                .downloaderVersion(documentLocation.getDownloaderVersion())
                .downloadResultCode(documentLocation.getDownloadResultCode())
                .build();
    }
}
