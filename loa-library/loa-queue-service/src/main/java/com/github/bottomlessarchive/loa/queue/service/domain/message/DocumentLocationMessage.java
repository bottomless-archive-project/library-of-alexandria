package com.github.bottomlessarchive.loa.queue.service.domain.message;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class DocumentLocationMessage {

    private final String sourceName;
    private final String documentLocation;
}
