package com.github.loa.document.service.entity.transformer;

import com.github.loa.document.service.domain.DocumentEntity;
import com.github.loa.document.repository.domain.DocumentDatabaseEntity;
import com.github.loa.document.service.domain.DocumentStatus;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class DocumentEntityTransformer {

    public List<DocumentEntity> transform(final List<DocumentDatabaseEntity> documentDatabaseEntities) {
        return documentDatabaseEntities.stream()
                .map(this::transform)
                .collect(Collectors.toList());
    }

    public DocumentEntity transform(final DocumentDatabaseEntity documentDatabaseEntity) {
        return DocumentEntity.builder()
                .id(documentDatabaseEntity.getId())
                .url(documentDatabaseEntity.getUrl())
                .status(DocumentStatus.valueOf(documentDatabaseEntity.getStatus()))
                .checksum(documentDatabaseEntity.getChecksum())
                .fileSize(documentDatabaseEntity.getFileSize())
                .downloadDate(documentDatabaseEntity.getDownloadDate())
                .downloaderVersion(documentDatabaseEntity.getDownloaderVersion())
                .build();
    }
}
