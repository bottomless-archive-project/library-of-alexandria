package com.github.bottomlessarchive.loa.document.service.domain;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum DocumentType {

    PDF("pdf", "application/pdf"),
    DOC("doc", "application/msword"),
    DOCX("docx", "application/vnd.openxmlformats-officedocument.wordprocessingml.document"),
    PPT("ppt", "application/vnd.ms-powerpoint"),
    PPTX("pptx", "application/vnd.openxmlformats-officedocument.presentationml.presentation"),
    XLS("xls", "application/vnd.ms-excel"),
    XLSX("xlsx", "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"),
    RTF("rtf", "application/rtf"),
    MOBI("mobi", "application/x-mobipocket-ebook"),
    EPUB("epub", "application/epub+zip"),
    FB2("fb2", "text/fb2+xml"),
    TXT("txt", "text/plain");

    private final String fileExtension;
    private final String mimeType;
}
