package com.github.loa.backend.view.document.response;

import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public class DocumentSearchResponse {

    private final long totalHitCount;
    private final List<SearchDocumentEntityResponse> searchHits;
}
