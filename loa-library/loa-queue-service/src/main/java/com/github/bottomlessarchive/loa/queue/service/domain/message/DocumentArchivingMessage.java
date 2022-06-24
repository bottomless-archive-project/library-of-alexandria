package com.github.bottomlessarchive.loa.queue.service.domain.message;

import lombok.Builder;
import lombok.Getter;

import java.util.Optional;

@Getter
@Builder
public class DocumentArchivingMessage {

    private final String id;
    private final String type;
    private final String source;
    private final String checksum;
    private final String sourceLocationId;
    private final long contentLength;

    public boolean hasSourceLocationId() {
        return sourceLocationId != null;
    }

    public Optional<String> getSourceLocationId() {
        return Optional.ofNullable(sourceLocationId);
    }
}
