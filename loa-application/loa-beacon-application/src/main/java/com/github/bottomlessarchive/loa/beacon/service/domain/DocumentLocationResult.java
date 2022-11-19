package com.github.bottomlessarchive.loa.beacon.service.domain;

import com.github.bottomlessarchive.loa.type.domain.DocumentType;
import com.github.bottomlessarchive.loa.url.service.downloader.domain.DownloadResult;
import lombok.Builder;

import java.util.UUID;

@Builder
public record DocumentLocationResult(

        String id,
        UUID documentId,
        String checksum,
        long size,
        DownloadResult resultType,
        String sourceName,
        DocumentType type
) {
}
