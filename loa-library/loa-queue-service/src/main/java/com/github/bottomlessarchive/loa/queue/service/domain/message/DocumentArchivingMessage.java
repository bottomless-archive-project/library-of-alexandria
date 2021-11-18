package com.github.bottomlessarchive.loa.queue.service.domain.message;

import lombok.Builder;
import lombok.Getter;
import lombok.ToString;

@Getter
@Builder
@ToString(exclude = "content")
public class DocumentArchivingMessage {

    private final String id;
    private final String type;
    private final String source;
    private final int contentLength;
    private final byte[] content;
}
