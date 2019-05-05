package com.github.loa.backend.view.document.service;

import com.github.loa.backend.view.document.response.SearchDocumentEntityResponse;
import com.github.loa.indexer.domain.SearchDocumentEntity;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class SearchDocumentEntityResponseTransformer {

    private final DocumentEntityResponseTransformer documentEntityResponseTransformer;

    public List<SearchDocumentEntityResponse> transform(final List<SearchDocumentEntity> searchDocumentEntity) {
        return searchDocumentEntity.stream()
                .map(this::transform)
                .collect(Collectors.toList());
    }

    public SearchDocumentEntityResponse transform(final SearchDocumentEntity searchDocumentEntity) {
        return SearchDocumentEntityResponse.builder()
                .author(searchDocumentEntity.getAuthor())
                .description(searchDocumentEntity.getDescription())
                .language(searchDocumentEntity.getLanguage())
                .title(searchDocumentEntity.getTitle())
                .documentEntityResponse(documentEntityResponseTransformer.transform(
                        searchDocumentEntity.getDocumentEntity()))
                .build();
    }
}
