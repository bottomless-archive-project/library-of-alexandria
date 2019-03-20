package com.github.loa.downloader.document.service.entity.factory.domain;

import com.github.loa.downloader.domain.DocumentStatus;
import lombok.Builder;
import lombok.Getter;

import java.net.URL;

@Getter
@Builder
public class DocumentCreationContext {

    private final String id;
    private final URL location;
    private final String crc;
    private final long fileSize;
    private final DocumentStatus status;
}
