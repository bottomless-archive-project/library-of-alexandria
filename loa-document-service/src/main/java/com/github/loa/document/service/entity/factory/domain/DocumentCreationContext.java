package com.github.loa.document.service.entity.factory.domain;

import com.github.loa.document.service.domain.DocumentStatus;
import lombok.Builder;
import lombok.Getter;

import java.net.URL;

@Getter
@Builder
public class DocumentCreationContext {

    private final String id;
    private final URL location;
    private final String checksum;
    private final long fileSize;
    private final DocumentStatus status;
    private final int versionNumber;
    private final String source;
}
