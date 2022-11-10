package com.github.bottomlessarchive.loa.web.view.document.response;

import com.github.bottomlessarchive.loa.compression.domain.DocumentCompression;
import com.github.bottomlessarchive.loa.document.service.domain.DocumentStatus;
import com.github.bottomlessarchive.loa.document.service.domain.DocumentType;
import lombok.Builder;
import lombok.Value;

import java.time.Instant;
import java.util.Set;
import java.util.UUID;

@Value
@Builder
public class DocumentDebugResponse {

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
    boolean isInVault;
    Set<String> sourceLocations;
}
