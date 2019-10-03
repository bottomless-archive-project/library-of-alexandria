package com.github.loa.document.service.domain;

import com.github.loa.compression.domain.DocumentCompression;
import lombok.Builder;
import lombok.Getter;

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
    private final int pageCount;
    private final DocumentCompression compression;

    /**
     * Returns true if the document is saved to the vault.
     *
     * @return true if the document is available in the vault, false otherwise
     */
    public boolean isArchived() {
        return status == DocumentStatus.DOWNLOADED || status == DocumentStatus.INDEXED;
    }

    /**
     * Returns true if the document is removed from the vault.
     *
     * @return true if the document is removed, false otherwise
     */
    public boolean isRemoved() {
        return status == DocumentStatus.REMOVED;
    }

    /**
     * Returns true if the document's type is {@link DocumentType#PDF}.
     *
     * @return true if the document's type is PDF, false otherwise
     */
    public boolean isPdf() {
        return type == DocumentType.PDF;
    }

    /**
     * Returns true if the document is compressed with a {@link DocumentCompression}.
     *
     * @return true if the document is compressed, false otherwise
     */
    public boolean isCompressed() {
        return compression != DocumentCompression.NONE;
    }
}
