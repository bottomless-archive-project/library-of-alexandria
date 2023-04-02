package com.github.bottomlessarchive.loa.web.view.location.service;

import com.github.bottomlessarchive.loa.location.service.factory.domain.DocumentLocation;
import com.github.bottomlessarchive.loa.web.view.location.response.DocumentLocationDebugResponse;
import org.springframework.stereotype.Service;

@Service
public class DocumentLocationDebugResponseFactory {

    public DocumentLocationDebugResponse newDocumentLocationDebugResponse(final DocumentLocation documentLocation) {
        return DocumentLocationDebugResponse.builder()
                .id(documentLocation.id())
                .url(documentLocation.url())
                .source(documentLocation.source())
                .downloaderVersion(documentLocation.downloaderVersion())
                .downloadResultCode(documentLocation.downloadResultCode())
                .build();
    }
}
