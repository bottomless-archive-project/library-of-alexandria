package com.github.loa.backend.view.document.controller;

import com.github.loa.backend.view.document.response.SearchDocumentEntityResponse;
import com.github.loa.backend.view.document.service.SearchDocumentEntityResponseTransformer;
import com.github.loa.indexer.domain.SearchDocumentEntity;
import com.github.loa.indexer.service.DocumentSearchService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class DocumentSearchController {

    private final DocumentSearchService documentSearchService;
    private final SearchDocumentEntityResponseTransformer documentEntityResponseTransformer;

    @GetMapping("/document/find-by/keyword/{keyword}")
    public List<SearchDocumentEntityResponse> queryDocuments(@PathVariable final String keyword) {
        final List<SearchDocumentEntity> documentEntities = documentSearchService.searchDocuments(keyword);

        return documentEntityResponseTransformer.transform(documentEntities);
    }
}
