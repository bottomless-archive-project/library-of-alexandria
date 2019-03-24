package com.github.loa.document.service.domain;

import lombok.Builder;
import lombok.Getter;

import java.time.Instant;

@Getter
@Builder
public class DocumentEntity {

    private final String id;
    private final String url;
    private final DocumentStatus status;
    private final Instant downloadDate;
    private final String checksum;
    private final long fileSize;
    private final int downloaderVersion;
}
