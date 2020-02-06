package com.github.loa.queue.service.domain.message;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class DocumentArchivingMessage {

    private String type;
    private String source;
    private int contentLength;
    private byte[] content;
}
