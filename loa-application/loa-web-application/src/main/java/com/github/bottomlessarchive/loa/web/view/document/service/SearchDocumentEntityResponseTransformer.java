package com.github.bottomlessarchive.loa.web.view.document.service;

import com.github.bottomlessarchive.loa.document.service.domain.DocumentEntity;
import com.github.bottomlessarchive.loa.document.service.entity.factory.DocumentEntityFactory;
import com.github.bottomlessarchive.loa.indexer.service.search.domain.DocumentSearchEntity;
import com.github.bottomlessarchive.loa.location.service.factory.DocumentLocationEntityFactory;
import com.github.bottomlessarchive.loa.location.service.factory.domain.DocumentLocation;
import com.github.bottomlessarchive.loa.web.view.document.response.SearchDocumentEntityResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class SearchDocumentEntityResponseTransformer {

    private final DocumentEntityFactory documentEntityFactory;
    private final DocumentLocationEntityFactory documentLocationEntityFactory;

    public List<SearchDocumentEntityResponse> transform(final List<DocumentSearchEntity> documentSearchEntity) {
        return documentSearchEntity.stream()
                .map(this::transform)
                .toList();
    }

    private SearchDocumentEntityResponse transform(final DocumentSearchEntity documentSearchEntity) {
        final DocumentEntity documentEntity = documentEntityFactory.getDocumentEntity(UUID.fromString(documentSearchEntity.getId()))
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR,
                        "Unable to find the document with id: " + documentSearchEntity.getId() + " in the database!"));

        return SearchDocumentEntityResponse.builder()
                .id(documentSearchEntity.getId())
                .author(documentSearchEntity.getAuthor())
                .description(documentSearchEntity.getDescription())
                .language(documentSearchEntity.getLanguage())
                .title(documentSearchEntity.getTitle())
                .pageCount(documentSearchEntity.getPageCount())
                .type(documentSearchEntity.getType())
                .vault(documentEntity.getVault())
                .source(documentEntity.getSource())
                .downloadDate(documentEntity.getDownloadDate())
                .sourceLocations(
                        documentEntity.getSourceLocations().stream()
                                .map(documentLocationEntityFactory::getDocumentLocation)
                                .flatMap(Optional::stream)
                                .map(DocumentLocation::getUrl)
                                .collect(Collectors.toSet())
                )
                .build();
    }
}
