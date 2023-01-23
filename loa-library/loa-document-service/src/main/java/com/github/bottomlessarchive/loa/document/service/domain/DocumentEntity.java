package com.github.bottomlessarchive.loa.document.service.domain;

import com.github.bottomlessarchive.loa.compression.domain.DocumentCompression;
import com.github.bottomlessarchive.loa.type.domain.DocumentType;
import lombok.Builder;
import lombok.Getter;
import lombok.ToString;

import java.time.Instant;
import java.util.Optional;
import java.util.Set;
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
    private final String source;
    private final String beacon;
    @ToString.Include
    private final Set<String> sourceLocations;

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
        return status == DocumentStatus.CORRUPT;
    }

    /**
     * Returns true if the document is compressed with a {@link DocumentCompression}.
     *
     * @return true if the document is compressed, false otherwise
     */
    public boolean isCompressed() {
        return compression != DocumentCompression.NONE;
    }

    public Optional<String> getBeacon() {
        return Optional.ofNullable(beacon);
    }

    public boolean isInVault(final String vault) {
        return this.vault.equals(vault);
    }
}
