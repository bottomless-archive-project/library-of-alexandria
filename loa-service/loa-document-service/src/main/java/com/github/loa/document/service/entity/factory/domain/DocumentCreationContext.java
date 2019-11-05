package com.github.loa.document.service.entity.factory.domain;

import com.github.loa.compression.domain.DocumentCompression;
import com.github.loa.document.service.domain.DocumentStatus;
import com.github.loa.document.service.domain.DocumentType;
import lombok.Builder;
import lombok.Getter;

import java.net.URL;

@Getter
@Builder
public class DocumentCreationContext {

    private final String id;
    private final DocumentType type;
    private final URL location;
    private final String source;
    private final String checksum;
    private final long fileSize;
    private final DocumentStatus status;
    private final DocumentCompression compression;
    private final int versionNumber;
}
