package com.github.bottomlessarchive.loa.document.view.service;

import com.github.bottomlessarchive.loa.type.domain.DocumentType;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;

@Service
public class MediaTypeCalculator {

    public MediaType calculateMediaType(final DocumentType documentType) {
        return MediaType.valueOf(documentType.getMimeType());
    }
}
