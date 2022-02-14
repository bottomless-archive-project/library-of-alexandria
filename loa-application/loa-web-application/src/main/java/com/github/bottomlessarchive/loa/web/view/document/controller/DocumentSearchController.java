package com.github.bottomlessarchive.loa.web.view.document.controller;

import com.github.bottomlessarchive.loa.document.service.domain.DocumentType;
import com.github.bottomlessarchive.loa.indexer.service.search.DocumentSearchClient;
import com.github.bottomlessarchive.loa.indexer.service.search.domain.DocumentLength;
import com.github.bottomlessarchive.loa.indexer.service.search.domain.DocumentSearchResult;
import com.github.bottomlessarchive.loa.indexer.service.search.domain.SearchContext;
import com.github.bottomlessarchive.loa.web.view.document.response.DocumentSearchResponse;
import com.github.bottomlessarchive.loa.web.view.document.service.SearchDocumentEntityResponseTransformer;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class DocumentSearchController {

    private final DocumentSearchClient documentSearchClient;
    private final SearchDocumentEntityResponseTransformer documentEntityResponseTransformer;

    @GetMapping("/document/find-by/keyword/{keyword}")
    public DocumentSearchResponse queryDocuments(@PathVariable final String keyword,
            @RequestParam(defaultValue = "0") final int pageNumber,
            @RequestParam(defaultValue = "10") final int resultSize,
            @RequestParam(defaultValue = "false") final boolean exactMatch,
            @RequestParam(required = false) final String language,
            @RequestParam(required = false) final DocumentLength documentLength,
            @RequestParam(required = false) final List<DocumentType> documentTypes) {
        if (pageNumber < 0) {
            throw new IllegalArgumentException("The page number can only be zero or a positive integer!");
        }

        if (resultSize > 100) {
            throw new IllegalArgumentException("The result size can't be higher than 100!");
        }

        if (resultSize < 10) {
            throw new IllegalArgumentException("The result size can't be lower than 10!");
        }

        final DocumentSearchResult documentSearchResult = documentSearchClient.searchDocuments(
                SearchContext.builder()
                        .keyword(keyword)
                        .pageNumber(pageNumber)
                        .exactMatch(exactMatch)
                        .language(language)
                        .resultSize(resultSize)
                        .documentLength(documentLength)
                        .documentTypes(documentTypes)
                        .build()
        );

        return DocumentSearchResponse.builder()
                .searchHits(documentEntityResponseTransformer.transform(documentSearchResult.getSearchHits()))
                .totalHitCount(documentSearchResult.getTotalHitCount())
                .build();
    }
}
