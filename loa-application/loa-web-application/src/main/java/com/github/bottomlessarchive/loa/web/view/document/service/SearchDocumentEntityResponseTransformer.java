package com.github.bottomlessarchive.loa.web.view.document.service;

import com.github.bottomlessarchive.loa.indexer.service.search.domain.DocumentSearchEntity;
import com.github.bottomlessarchive.loa.web.view.document.response.SearchDocumentEntityResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class SearchDocumentEntityResponseTransformer {

    public List<SearchDocumentEntityResponse> transform(final List<DocumentSearchEntity> documentSearchEntity) {
        return documentSearchEntity.stream()
                .map(this::transform)
                .toList();
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
