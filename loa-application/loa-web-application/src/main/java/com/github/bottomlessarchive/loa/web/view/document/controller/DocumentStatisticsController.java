package com.github.bottomlessarchive.loa.web.view.document.controller;

import com.github.bottomlessarchive.loa.document.service.entity.factory.DocumentEntityFactory;
import com.github.bottomlessarchive.loa.indexer.service.search.DocumentSearchClient;
import com.github.bottomlessarchive.loa.web.view.document.response.DocumentStatisticsResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class DocumentStatisticsController {

    private final DocumentEntityFactory documentEntityFactory;
    private final DocumentSearchClient documentSearchClient;

    @GetMapping("/statistics")
    public DocumentStatisticsResponse documentStatistics() {
        return DocumentStatisticsResponse.builder()
                .documentCount(documentEntityFactory.getEstimatedDocumentCount())
                .indexedDocumentCount(documentSearchClient.countDocuments())
                .build();
    }
}
