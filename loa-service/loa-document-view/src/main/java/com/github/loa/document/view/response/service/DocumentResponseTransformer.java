package com.github.loa.document.view.response.service;

import com.github.loa.document.service.domain.DocumentEntity;
import com.github.loa.document.view.response.domain.DocumentResponse;
import org.springframework.stereotype.Service;

@Service
public class DocumentResponseTransformer {

    public DocumentResponse transform(final DocumentEntity documentEntity) {
        return DocumentResponse.builder()
                .id(documentEntity.getId().toString())
                .status(documentEntity.getStatus())
                .type(documentEntity.getType())
                .checksum(documentEntity.getChecksum())
                .compression(documentEntity.getCompression())
                .downloadDate(documentEntity.getDownloadDate())
                .downloaderVersion(documentEntity.getDownloaderVersion())
                .fileSize(documentEntity.getFileSize())
                .build();
    }
}
