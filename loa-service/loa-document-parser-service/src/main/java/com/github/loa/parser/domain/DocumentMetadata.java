package com.github.loa.parser.domain;

import com.github.loa.document.service.domain.DocumentType;
import lombok.Builder;
import lombok.Getter;

import java.util.UUID;

@Getter
@Builder
public class DocumentMetadata {

    private final UUID id;
    private final String content;
    private final String title;
    private final String author;
    private final String date;
    private final String language;
    private final DocumentType type;
    private final int pageCount;
}
