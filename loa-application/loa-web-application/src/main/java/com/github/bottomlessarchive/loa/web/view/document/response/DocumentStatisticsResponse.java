package com.github.bottomlessarchive.loa.web.view.document.response;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class DocumentStatisticsResponse {

    private final long documentCount;
    private final long indexedDocumentCount;
}
