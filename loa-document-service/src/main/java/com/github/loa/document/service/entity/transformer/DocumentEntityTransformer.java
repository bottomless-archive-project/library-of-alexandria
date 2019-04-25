package com.github.loa.document.service.entity.transformer;

import com.github.loa.compression.domain.DocumentCompression;
import com.github.loa.document.service.domain.DocumentEntity;
import com.github.loa.document.repository.domain.DocumentDatabaseEntity;
import com.github.loa.document.service.domain.DocumentStatus;
import org.springframework.stereotype.Service;

import java.net.MalformedURLException;
import java.net.URL;
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
        try {
            return DocumentEntity.builder()
                    .id(documentDatabaseEntity.getId())
                    .url(new URL(documentDatabaseEntity.getUrl()))
                    .status(DocumentStatus.valueOf(documentDatabaseEntity.getStatus()))
                    .checksum(documentDatabaseEntity.getChecksum())
                    .fileSize(documentDatabaseEntity.getFileSize())
                    .downloadDate(documentDatabaseEntity.getDownloadDate())
                    .downloaderVersion(documentDatabaseEntity.getDownloaderVersion())
                    .compression(DocumentCompression.valueOf(documentDatabaseEntity.getCompression()))
                    .build();
        } catch (MalformedURLException e) {
            throw new RuntimeException("Unable to transform entity because of a bad URL: "
                    + documentDatabaseEntity.getUrl() + "!", e);
        }
    }
}
