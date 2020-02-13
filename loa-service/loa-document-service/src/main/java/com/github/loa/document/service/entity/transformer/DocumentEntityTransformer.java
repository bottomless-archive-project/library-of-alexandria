package com.github.loa.document.service.entity.transformer;

import com.github.loa.compression.domain.DocumentCompression;
import com.github.loa.document.repository.domain.DocumentDatabaseEntity;
import com.github.loa.document.service.domain.DocumentEntity;
import com.github.loa.document.service.domain.DocumentStatus;
import com.github.loa.document.service.domain.DocumentType;
import com.github.loa.repository.service.HexConverter;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class DocumentEntityTransformer {

    private final HexConverter hexConverter;

    public DocumentEntity transform(final DocumentDatabaseEntity documentDatabaseEntity) {
        return DocumentEntity.builder()
                .id(documentDatabaseEntity.getId())
                .type(DocumentType.valueOf(documentDatabaseEntity.getType()))
                .status(DocumentStatus.valueOf(documentDatabaseEntity.getStatus()))
                .checksum(hexConverter.encode(documentDatabaseEntity.getChecksum()))
                .fileSize(documentDatabaseEntity.getFileSize())
                .downloadDate(documentDatabaseEntity.getDownloadDate())
                .downloaderVersion(documentDatabaseEntity.getDownloaderVersion())
                .compression(DocumentCompression.valueOf(documentDatabaseEntity.getCompression()))
                .build();
    }
}
