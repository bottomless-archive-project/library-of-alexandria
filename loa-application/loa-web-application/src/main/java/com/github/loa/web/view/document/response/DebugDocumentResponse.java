package com.github.loa.web.view.document.response;

import com.github.loa.compression.domain.DocumentCompression;
import com.github.loa.document.service.domain.DocumentStatus;
import com.github.loa.document.service.domain.DocumentType;
import lombok.Builder;
import lombok.Value;

import java.time.Instant;
import java.util.UUID;

@Value
@Builder
public class DebugDocumentResponse {

    UUID id;
    String vault;
    DocumentType type;
    DocumentStatus status;
    DocumentCompression compression;
    String checksum;
    long fileSize;
    Instant downloadDate;
    int downloaderVersion;

    boolean isInIndex;
}
