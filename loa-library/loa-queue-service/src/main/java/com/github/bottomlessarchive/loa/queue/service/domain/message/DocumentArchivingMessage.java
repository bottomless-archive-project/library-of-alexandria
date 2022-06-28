package com.github.bottomlessarchive.loa.queue.service.domain.message;

import lombok.Builder;
import lombok.Getter;
import lombok.ToString;

import java.util.Optional;

@Getter
@Builder
@ToString
public class DocumentArchivingMessage {

    private final String id;
    private final String type;
    private final String source;
    private final String checksum;
    private final String sourceLocationId;
    private final long contentLength;
    private final long originalContentLength;
    private final String compression;

    public boolean hasSourceLocationId() {
        return sourceLocationId != null;
    }

    public Optional<String> getSourceLocationId() {
        return Optional.ofNullable(sourceLocationId);
    }
}
