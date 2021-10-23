package com.github.bottomlessarchive.loa.queue.service.domain.message;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class DocumentArchivingMessage {

    private final String id;
    private final String type;
    private final String source;
    private final int contentLength;
    private final byte[] content;
}
