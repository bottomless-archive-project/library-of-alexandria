package com.github.loa.document.service.domain;

import com.github.loa.compression.domain.DocumentCompression;
import lombok.Builder;
import lombok.Getter;

import java.net.URL;
import java.time.Instant;

@Getter
@Builder
public class DocumentEntity {

    private final String id;
    private final DocumentStatus status;
    private final DocumentType type;
    private final Instant downloadDate;
    private final String checksum;
    private final long fileSize;
    private final int downloaderVersion;
    private final DocumentCompression compression;

    /**
     * Returns true if the document is saved to the vault.
     *
     * @return true if the document is available in the vault, false otherwise
     */
    public boolean isInVault() {
        return status == DocumentStatus.DOWNLOADED || status == DocumentStatus.INDEXED;
    }
}
