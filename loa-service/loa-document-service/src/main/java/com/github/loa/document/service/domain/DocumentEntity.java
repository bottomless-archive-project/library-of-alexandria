package com.github.loa.document.service.domain;

import com.github.loa.compression.domain.DocumentCompression;
import lombok.Builder;
import lombok.Getter;
import lombok.ToString;

import java.time.Instant;
import java.util.UUID;

@Getter
@Builder
@ToString(onlyExplicitlyIncluded = true)
public class DocumentEntity {

    @ToString.Include
    private final UUID id;
    @ToString.Include
    private final String vault;
    @ToString.Include
    private final DocumentType type;
    @ToString.Include
    private final DocumentStatus status;
    private final Instant downloadDate;
    private final String checksum;
    private final long fileSize;
    private final int downloaderVersion;
    @ToString.Include
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
     * Returns true if the document is not available in the vault, or the data that is available are corrupt in any for or shape. This
     * means that a corrupt document mostly likely can't be opened, indexed or restored.
     *
     * @return true if the document is corrupt in the vault, false otherwise
     */
    public boolean isCorrupt() {
        return status == DocumentStatus.CORRUPT || status == DocumentStatus.INDEXING_FAILURE;
    }

    /**
     * Returns true if the document is compressed with a {@link DocumentCompression}.
     *
     * @return true if the document is compressed, false otherwise
     */
    public boolean isCompressed() {
        return compression != DocumentCompression.NONE;
    }

    public boolean isInVault(final String vault) {
        return this.vault.equals(vault);
    }
}
