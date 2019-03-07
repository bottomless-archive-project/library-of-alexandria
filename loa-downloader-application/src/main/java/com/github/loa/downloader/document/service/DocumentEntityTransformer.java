package com.github.loa.downloader.document.service;

import com.github.loa.downloader.document.domain.DocumentEntity;
import com.github.loa.downloader.repository.domain.DocumentDatabaseEntity;
import org.springframework.stereotype.Service;

@Service
public class DocumentEntityTransformer {

    public DocumentEntity transform(final DocumentDatabaseEntity tomeDatabaseEntity) {
        return DocumentEntity.builder()
                .id(tomeDatabaseEntity.getId())
                .url(tomeDatabaseEntity.getUrl())
                .status(tomeDatabaseEntity.getStatus())
                .crc(tomeDatabaseEntity.getCrc())
                .fileSize(tomeDatabaseEntity.getFileSize())
                .downloadDate(tomeDatabaseEntity.getDownloadDate())
                .downloaderVersion(tomeDatabaseEntity.getDownloaderVersion())
                .build();
    }
}
