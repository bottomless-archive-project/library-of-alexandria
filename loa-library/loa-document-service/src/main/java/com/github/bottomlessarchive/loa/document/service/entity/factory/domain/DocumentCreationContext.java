package com.github.bottomlessarchive.loa.document.service.entity.factory.domain;

import com.github.bottomlessarchive.loa.compression.domain.DocumentCompression;
import com.github.bottomlessarchive.loa.document.service.domain.DocumentStatus;
import com.github.bottomlessarchive.loa.type.domain.DocumentType;
import lombok.Builder;
import lombok.NonNull;

import java.util.Optional;
import java.util.UUID;

@Builder
public record DocumentCreationContext(

        @NonNull
        UUID id,

        @NonNull
        String vault,

        @NonNull
        DocumentType type,

        @NonNull
        String source,

        @NonNull
        Optional<String> sourceLocationId,

        @NonNull
        String checksum,

        @NonNull
        DocumentStatus status,

        @NonNull
        DocumentCompression compression,

        String beacon,
        long fileSize,
        int versionNumber
) {
}
