package com.github.loa.vault.client.service.domain;

import com.github.loa.document.service.domain.DocumentType;
import lombok.Builder;
import lombok.Getter;

import java.nio.file.Path;

@Getter
@Builder
public class ArchivingContext {

    private DocumentType type;
    private String location;
    private String source;
    private Path contents;
}
