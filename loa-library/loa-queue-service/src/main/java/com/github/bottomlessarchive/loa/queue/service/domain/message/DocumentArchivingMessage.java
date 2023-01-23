package com.github.bottomlessarchive.loa.queue.service.domain.message;

import lombok.Builder;
import lombok.NonNull;

import java.util.Optional;

@Builder
public record DocumentArchivingMessage(

        @NonNull
        String id,

        @NonNull
        String type,

        @NonNull
        String source,

        @NonNull
        String checksum,

        @NonNull
        Optional<String> sourceLocationId,

        @NonNull
        String compression,

        boolean fromBeacon,
        long contentLength,
        long originalContentLength
) {
}
