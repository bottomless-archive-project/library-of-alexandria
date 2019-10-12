package com.github.loa.indexer.command.domain;

import com.github.loa.document.service.domain.DocumentEntity;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class IndexDocument {

    private final DocumentEntity documentEntity;
    private final byte[] documentContents;
}
