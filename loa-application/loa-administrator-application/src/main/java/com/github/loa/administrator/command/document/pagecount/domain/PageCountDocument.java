package com.github.loa.administrator.command.document.pagecount.domain;

import com.github.loa.document.service.domain.DocumentEntity;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class PageCountDocument {

    private final DocumentEntity documentEntity;
    private final byte[] documentContents;
}
