package com.github.loa.document.view.service;

import com.github.loa.document.service.domain.DocumentType;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;

@Service
public class MediaTypeCalculator {

    public MediaType calculateMediaType(final DocumentType documentType) {
        switch (documentType) {
            case PDF:
                return MediaType.APPLICATION_PDF;
            case DOC:
                return MediaType.valueOf("application/msword");
            case PPT:
                return MediaType.valueOf("application/vnd.ms-powerpoint");
            case RTF:
                return MediaType.valueOf("application/rtf");
            case XLS:
                return MediaType.valueOf("application/vnd.ms-excel");
            case DOCX:
                return MediaType.valueOf("application/vnd.openxmlformats-officedocument.wordprocessingml.document");
            case PPTX:
                return MediaType.valueOf("application/vnd.openxmlformats-officedocument.presentationml.presentation");
            case XLSX:
                return MediaType.valueOf("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
            case EPUB:
                return MediaType.valueOf("application/epub+zip");
            case MOBI:
                return MediaType.valueOf("application/x-mobipocket-ebook");
            default:
                throw new RuntimeException("Unknown document type!");
        }
    }
}
