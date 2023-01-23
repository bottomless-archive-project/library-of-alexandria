package com.github.bottomlessarchive.loa.document.service.entity.transformer;

import com.github.bottomlessarchive.loa.compression.domain.DocumentCompression;
import com.github.bottomlessarchive.loa.number.service.HexConverter;
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
                .id(documentDatabaseEntity.id())
                .vault(documentDatabaseEntity.vault())
                .type(DocumentType.valueOf(documentDatabaseEntity.type()))
                .status(DocumentStatus.valueOf(documentDatabaseEntity.status()))
                .checksum(hexConverter.encode(documentDatabaseEntity.checksum()))
                .fileSize(documentDatabaseEntity.fileSize())
                .downloadDate(documentDatabaseEntity.downloadDate())
                .downloaderVersion(documentDatabaseEntity.downloaderVersion())
                .compression(DocumentCompression.valueOf(documentDatabaseEntity.compression()))
                .source(documentDatabaseEntity.source())
                .beacon(documentDatabaseEntity.beacon())
                .sourceLocations(documentDatabaseEntity.sourceLocations().stream()
                        .map(hexConverter::encode)
                        .collect(Collectors.toSet())
                )
                .build();
    }
}
