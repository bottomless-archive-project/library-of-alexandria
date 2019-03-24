package com.github.loa.document.service;

import com.github.loa.document.service.domain.DocumentEntity;
import com.github.loa.document.repository.domain.DocumentDatabaseEntity;
import com.github.loa.document.service.domain.DocumentStatus;
import org.springframework.stereotype.Service;

@Service
public class DocumentEntityTransformer {

    public DocumentEntity transform(final DocumentDatabaseEntity tomeDatabaseEntity) {
        return DocumentEntity.builder()
                .id(tomeDatabaseEntity.getId())
                .url(tomeDatabaseEntity.getUrl())
                .status(DocumentStatus.valueOf(tomeDatabaseEntity.getStatus()))
                .checksum(tomeDatabaseEntity.getChecksum())
                .fileSize(tomeDatabaseEntity.getFileSize())
                .downloadDate(tomeDatabaseEntity.getDownloadDate())
                .downloaderVersion(tomeDatabaseEntity.getDownloaderVersion())
                .build();
    }
}
