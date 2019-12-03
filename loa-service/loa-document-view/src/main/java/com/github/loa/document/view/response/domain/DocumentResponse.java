package com.github.loa.document.view.response.domain;

import com.github.loa.compression.domain.DocumentCompression;
import com.github.loa.document.service.domain.DocumentStatus;
import com.github.loa.document.service.domain.DocumentType;
import lombok.Builder;
import lombok.Getter;

import java.time.Instant;

@Getter
@Builder
public class DocumentResponse {

    private final String id;
    private final DocumentType type;
    private final DocumentStatus status;
    private final Instant downloadDate;
    private final String checksum;
    private final long fileSize;
    private final int downloaderVersion;
    private final DocumentCompression compression;
}
