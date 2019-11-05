package com.github.loa.web.view.document.service;

import com.github.loa.indexer.domain.DocumentSearchEntity;
import com.github.loa.web.view.document.response.SearchDocumentEntityResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class SearchDocumentEntityResponseTransformer {

    public List<SearchDocumentEntityResponse> transform(final List<DocumentSearchEntity> documentSearchEntity) {
        return documentSearchEntity.stream()
                .map(this::transform)
                .collect(Collectors.toList());
    }

    public SearchDocumentEntityResponse transform(final DocumentSearchEntity documentSearchEntity) {
        return SearchDocumentEntityResponse.builder()
                .id(documentSearchEntity.getId())
                .author(documentSearchEntity.getAuthor())
                .description(documentSearchEntity.getDescription())
                .language(documentSearchEntity.getLanguage())
                .title(documentSearchEntity.getTitle())
                .pageCount(documentSearchEntity.getPageCount())
                .type(documentSearchEntity.getType())
                .build();
    }
}
