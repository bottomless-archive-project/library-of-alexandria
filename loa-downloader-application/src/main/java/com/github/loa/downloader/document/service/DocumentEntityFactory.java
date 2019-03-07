package com.github.loa.downloader.document.service;

import com.github.loa.downloader.document.domain.DocumentEntity;
import com.github.loa.downloader.repository.DocumentRepository;
import com.github.loa.downloader.repository.domain.DocumentDatabaseEntity;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class DocumentEntityFactory {

    private final DocumentRepository documentRepository;
    private final DocumentEntityTransformer documentEntityTransformer;

    public boolean isDocumentExists(final String documentId) {
        return documentRepository.findById(documentId) != null;
    }

    /**
     * Return the document entities belonging to the provided crc and file size values.
     *
     * @param crc      the crc value of the document
     * @param fileSize the file size value of the document
     * @return the list of documents with the provided values
     */
    public List<DocumentEntity> getDocumentEntity(final String crc, final long fileSize) {
        final List<DocumentDatabaseEntity> tomeDatabaseEntity = documentRepository.findByCrcAndFileSize(crc, fileSize);

        return tomeDatabaseEntity.stream()
                .map(documentEntityTransformer::transform)
                .collect(Collectors.toList());
    }
}
