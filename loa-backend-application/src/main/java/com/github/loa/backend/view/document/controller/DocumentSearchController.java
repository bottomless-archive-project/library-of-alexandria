package com.github.loa.backend.view.document.controller;

import com.github.loa.backend.view.document.response.DocumentSearchResponse;
import com.github.loa.backend.view.document.service.SearchDocumentEntityResponseTransformer;
import com.github.loa.indexer.domain.DocumentSearchResult;
import com.github.loa.indexer.service.DocumentSearchService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class DocumentSearchController {

    private final DocumentSearchService documentSearchService;
    private final SearchDocumentEntityResponseTransformer documentEntityResponseTransformer;

    @GetMapping("/document/find-by/keyword/{keyword}")
    public DocumentSearchResponse queryDocuments(@PathVariable final String keyword,
                                                 @RequestParam(defaultValue = "0") final int pageNumber) {
        final DocumentSearchResult documentSearchResult = documentSearchService.searchDocuments(keyword, pageNumber);

        return DocumentSearchResponse.builder()
                .searchHits(documentEntityResponseTransformer.transform(documentSearchResult.getSearchHits()))
                .totalHitCount(documentSearchResult.getTotalHitCount())
                .build();
    }
}
