package com.github.bottomlessarchive.loa.vault.service.domain;

import com.github.bottomlessarchive.loa.document.service.domain.DocumentType;
import lombok.Builder;
import lombok.Getter;

import java.io.InputStream;
import java.util.Optional;
import java.util.UUID;

@Getter
@Builder
public class DocumentArchivingContext {

    private final UUID id;
    private final String vault;
    private final DocumentType type;
    private final String source;
    private final String sourceLocationId;
    private final long contentLength;
    private final String checksum;
    private final InputStream content;
    private final int versionNumber;

    public Optional<String> getSourceLocationId() {
        return Optional.ofNullable(sourceLocationId);
    }
}
