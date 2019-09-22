package com.github.loa.web.view.document.controller;

import com.github.loa.document.service.entity.factory.DocumentEntityFactory;
import com.github.loa.indexer.service.search.DocumentSearchService;
import com.github.loa.web.view.document.response.DocumentStatisticsResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

@RestController
@RequiredArgsConstructor
public class DocumentStatisticsController {

    private final DocumentEntityFactory documentEntityFactory;
    private final DocumentSearchService documentSearchService;

    @GetMapping("/statistics")
    public Mono<DocumentStatisticsResponse> documentStatistics() {
        return documentEntityFactory.getDocumentCount()
                .map(count -> DocumentStatisticsResponse.builder()
                        .documentCount(count)
                        .indexedDocumentCount(documentSearchService.countDocuments())
                        .build()
                );
    }
}
