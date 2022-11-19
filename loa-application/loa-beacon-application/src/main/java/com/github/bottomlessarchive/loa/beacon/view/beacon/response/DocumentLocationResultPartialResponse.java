package com.github.bottomlessarchive.loa.beacon.view.beacon.response;

import com.github.bottomlessarchive.loa.type.domain.DocumentType;
import com.github.bottomlessarchive.loa.url.service.downloader.domain.DownloadResult;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class DocumentLocationResultPartialResponse {

    private final String id;
    private final String documentId;
    private final String checksum;
    private final long size;
    private final DownloadResult resultType;
    private final String sourceName;
    private final DocumentType type;
}
