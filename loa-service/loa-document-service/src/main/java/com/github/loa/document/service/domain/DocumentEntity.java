package com.github.loa.document.service.domain;

import com.github.loa.compression.domain.DocumentCompression;
import lombok.Builder;
import lombok.Getter;
import lombok.ToString;

import java.time.Instant;

@Getter
@Builder
@ToString(onlyExplicitlyIncluded = true)
public class DocumentEntity {

    @ToString.Include
    private final String id;
    @ToString.Include
    private final DocumentType type;
    private final DocumentStatus status;
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
    public boolean isArchived() {
        return status == DocumentStatus.DOWNLOADED || status == DocumentStatus.INDEXED;
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
