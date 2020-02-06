package com.github.loa.vault.service.domain;

import com.github.loa.document.service.domain.DocumentType;
import lombok.Builder;
import lombok.Getter;

import java.io.InputStream;

@Getter
@Builder
public class DocumentArchivingContext {

    private DocumentType type;
    private String source;
    private int contentLength;
    private byte[] content;
}
