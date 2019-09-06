package com.github.loa.web.view.document.controller;

import com.github.loa.indexer.service.search.domain.SearchContext;
import com.github.loa.web.view.document.response.DocumentSearchResponse;
import com.github.loa.web.view.document.service.SearchDocumentEntityResponseTransformer;
import com.github.loa.indexer.domain.DocumentSearchResult;
import com.github.loa.indexer.service.search.DocumentSearchService;
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
            @RequestParam(defaultValue = "0") final int pageNumber,
            @RequestParam(defaultValue = "false") final boolean exactMatch,
            @RequestParam(required = false) final String language) {
        final DocumentSearchResult documentSearchResult = documentSearchService.searchDocuments(
                SearchContext.builder()
                        .keyword(keyword)
                        .pageNumber(pageNumber)
                        .exactMatch(exactMatch)
                        .language(language)
                        .build()
        );

        return DocumentSearchResponse.builder()
                .searchHits(documentEntityResponseTransformer.transform(documentSearchResult.getSearchHits()))
                .totalHitCount(documentSearchResult.getTotalHitCount())
                .build();
    }
}
