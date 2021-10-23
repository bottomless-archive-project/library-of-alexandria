package com.github.bottomlessarchive.loa.downloader.service.document.domain;

import com.github.bottomlessarchive.loa.document.service.domain.DocumentType;
import lombok.Builder;
import lombok.Getter;

import java.nio.file.Path;
import java.util.UUID;

@Getter
@Builder
public class DocumentArchivingContext {

    private final UUID id;
    private final DocumentType type;
    private final String source;
    private final Path contents;
}
