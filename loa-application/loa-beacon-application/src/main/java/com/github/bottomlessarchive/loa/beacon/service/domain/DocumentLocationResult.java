package com.github.bottomlessarchive.loa.beacon.service.domain;

import com.github.bottomlessarchive.loa.type.domain.DocumentType;
import com.github.bottomlessarchive.loa.url.service.downloader.domain.DownloadResult;
import lombok.Builder;

@Builder
public record DocumentLocationResult(

        String id,
        String checksum,
        long size,
        DownloadResult resultType,
        String sourceName,
        DocumentType type
) {
}
