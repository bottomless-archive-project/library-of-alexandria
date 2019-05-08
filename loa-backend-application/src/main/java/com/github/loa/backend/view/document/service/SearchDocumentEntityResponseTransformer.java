package com.github.loa.backend.view.document.service;

import com.github.loa.backend.view.document.response.SearchDocumentEntityResponse;
import com.github.loa.indexer.domain.DocumentSearchEntity;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class SearchDocumentEntityResponseTransformer {

    private final DocumentEntityResponseTransformer documentEntityResponseTransformer;

    public List<SearchDocumentEntityResponse> transform(final List<DocumentSearchEntity> documentSearchEntity) {
        return documentSearchEntity.stream()
                .map(this::transform)
                .collect(Collectors.toList());
    }

    public SearchDocumentEntityResponse transform(final DocumentSearchEntity documentSearchEntity) {
        return SearchDocumentEntityResponse.builder()
                .author(documentSearchEntity.getAuthor())
                .description(documentSearchEntity.getDescription())
                .language(documentSearchEntity.getLanguage())
                .title(documentSearchEntity.getTitle())
                .document(documentEntityResponseTransformer.transform(documentSearchEntity.getDocumentEntity()))
                .build();
    }
}
