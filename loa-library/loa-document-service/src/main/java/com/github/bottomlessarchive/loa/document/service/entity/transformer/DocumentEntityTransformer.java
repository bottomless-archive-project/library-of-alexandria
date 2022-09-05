package com.github.bottomlessarchive.loa.document.service.entity.transformer;

import com.github.bottomlessarchive.loa.compression.domain.DocumentCompression;
import com.github.bottomlessarchive.loa.repository.service.HexConverter;
import com.github.bottomlessarchive.loa.document.repository.domain.DocumentDatabaseEntity;
import com.github.bottomlessarchive.loa.document.service.domain.DocumentEntity;
import com.github.bottomlessarchive.loa.document.service.domain.DocumentStatus;
import com.github.bottomlessarchive.loa.type.domain.DocumentType;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class DocumentEntityTransformer {

    private final HexConverter hexConverter;

    public DocumentEntity transform(final DocumentDatabaseEntity documentDatabaseEntity) {
        return DocumentEntity.builder()
                .id(documentDatabaseEntity.getId())
                .vault(documentDatabaseEntity.getVault())
                .type(DocumentType.valueOf(documentDatabaseEntity.getType()))
                .status(DocumentStatus.valueOf(documentDatabaseEntity.getStatus()))
                .checksum(hexConverter.encode(documentDatabaseEntity.getChecksum()))
                .fileSize(documentDatabaseEntity.getFileSize())
                .downloadDate(documentDatabaseEntity.getDownloadDate())
                .downloaderVersion(documentDatabaseEntity.getDownloaderVersion())
                .compression(DocumentCompression.valueOf(documentDatabaseEntity.getCompression()))
                .source(documentDatabaseEntity.getSource())
                .sourceLocations(documentDatabaseEntity.getSourceLocations().stream()
                        .map(hexConverter::encode)
                        .collect(Collectors.toSet())
                )
                .build();
    }
}
