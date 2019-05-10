package com.github.loa.backend.view.document.service;

import com.github.loa.backend.view.document.response.DocumentEntityResponse;
import com.github.loa.document.service.domain.DocumentEntity;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class DocumentEntityResponseTransformer {

    public List<DocumentEntityResponse> transform(final List<DocumentEntity> documentEntities) {
        return documentEntities.stream()
                .map(this::transform)
                .collect(Collectors.toList());
    }

    public DocumentEntityResponse transform(final DocumentEntity documentEntity) {
        return DocumentEntityResponse.builder()
                .id(documentEntity.getId())
                .url(documentEntity.getUrl())
                .status(documentEntity.getStatus())
                .downloadDate(documentEntity.getDownloadDate())
                .build();
    }
}
