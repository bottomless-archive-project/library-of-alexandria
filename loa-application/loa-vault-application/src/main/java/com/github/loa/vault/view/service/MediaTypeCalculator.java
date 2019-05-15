package com.github.loa.vault.view.service;

import com.github.loa.document.service.domain.DocumentType;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
public class MediaTypeCalculator {

    private final Map<DocumentType, MediaType> mediaTypeMap;

    private MediaTypeCalculator() {
        mediaTypeMap = Map.ofEntries(
                Map.entry(DocumentType.PDF, MediaType.APPLICATION_PDF),
                Map.entry(DocumentType.DOC, MediaType.valueOf("application/msword")),
                Map.entry(DocumentType.DOCX, MediaType.valueOf("application/vnd.openxmlformats-officedocument.wordprocessingml.document")),
                Map.entry(DocumentType.PPT, MediaType.valueOf("application/vnd.ms-powerpoint")),
                Map.entry(DocumentType.PPTX, MediaType.valueOf("application/vnd.openxmlformats-officedocument.presentationml.presentation")),
                Map.entry(DocumentType.XLS, MediaType.valueOf("application/vnd.ms-excel")),
                Map.entry(DocumentType.XLSX, MediaType.valueOf("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet")),
                Map.entry(DocumentType.RTF, MediaType.valueOf("application/rtf")),
                Map.entry(DocumentType.MOBI, MediaType.valueOf("application/x-mobipocket-ebook")),
                Map.entry(DocumentType.EPUB, MediaType.valueOf("application/epub+zip"))
        );
    }

    public MediaType calculateMediaType(final DocumentType documentType) {
        return mediaTypeMap.get(documentType);
    }
}
