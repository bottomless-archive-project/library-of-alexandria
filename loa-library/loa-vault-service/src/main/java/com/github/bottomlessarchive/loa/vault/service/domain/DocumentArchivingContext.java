package com.github.bottomlessarchive.loa.vault.service.domain;

import com.github.bottomlessarchive.loa.compression.domain.DocumentCompression;
import com.github.bottomlessarchive.loa.type.domain.DocumentType;
import lombok.Builder;
import lombok.Getter;
import lombok.NonNull;

import java.util.Optional;
import java.util.UUID;

@Builder
public record DocumentArchivingContext(

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
        DocumentCompression compression,

        boolean fromBeacon,
        long contentLength,
        long originalContentLength,
        int versionNumber
) {
}
