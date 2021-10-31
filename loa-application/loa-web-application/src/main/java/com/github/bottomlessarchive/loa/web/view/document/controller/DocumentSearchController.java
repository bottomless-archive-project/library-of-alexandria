package com.github.bottomlessarchive.loa.web.view.document.controller;

import com.github.bottomlessarchive.loa.web.view.document.service.SearchDocumentEntityResponseTransformer;
import com.github.bottomlessarchive.loa.document.service.domain.DocumentType;
import com.github.bottomlessarchive.loa.indexer.service.search.DocumentSearchService;
import com.github.bottomlessarchive.loa.indexer.service.search.domain.DocumentLength;
import com.github.bottomlessarchive.loa.indexer.service.search.domain.SearchContext;
import com.github.bottomlessarchive.loa.web.view.document.response.DocumentSearchResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.util.List;
import java.util.function.Function;

@RestController
@RequiredArgsConstructor
public class DocumentSearchController {

    private final DocumentSearchService documentSearchService;
    private final SearchDocumentEntityResponseTransformer documentEntityResponseTransformer;

    @GetMapping("/document/find-by/keyword/{keyword}")
    public Mono<DocumentSearchResponse> queryDocuments(@PathVariable final String keyword,
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

        return Mono.fromSupplier(() -> documentSearchService.searchDocuments(
                        SearchContext.builder()
                                .keyword(keyword)
                                .pageNumber(pageNumber)
                                .exactMatch(exactMatch)
                                .language(language)
                                .resultSize(resultSize)
                                .documentLength(documentLength)
                                .documentTypes(documentTypes)
                                .build()
                ))
                .subscribeOn(Schedulers.boundedElastic())
                .map(documentSearchResult -> documentSearchResult.getSearchHits()
                        .collectList()
                        .map(documentSearchEntities -> DocumentSearchResponse.builder()
                                .searchHits(documentEntityResponseTransformer.transform(documentSearchEntities))
                                .totalHitCount(documentSearchResult.getTotalHitCount())
                                .build()
                        )
                )
                .flatMap(Function.identity());
    }
}
