package com.github.loa.downloader.document.domain;

import com.github.loa.downloader.domain.DocumentStatus;
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
    private final String crc;
    private final long fileSize;
    private final int downloaderVersion;
}
