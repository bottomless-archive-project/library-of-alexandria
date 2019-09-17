package com.github.loa.document.service.entity.factory;

import com.github.loa.document.repository.DocumentRepository;
import com.github.loa.document.repository.domain.DocumentDatabaseEntity;
import com.github.loa.document.service.domain.DocumentEntity;
import com.github.loa.document.service.domain.DocumentStatus;
import com.github.loa.document.service.domain.DocumentType;
import com.github.loa.document.service.entity.factory.domain.DocumentCreationContext;
import com.github.loa.document.service.entity.transformer.DocumentEntityTransformer;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Instant;

@Service
@RequiredArgsConstructor
public class DocumentEntityFactory {

    private final DocumentRepository documentRepository;
    private final DocumentEntityTransformer documentEntityTransformer;

    /**
     * Return true if any document exists with the provided checksum and file size.
     *
     * @param checksum the checksum to use for the checking
     * @param fileSize the file size used for the checking
     * @return return true if a document exist with the provided parameters or false otherwise
     */
    public boolean isDocumentExists(final String checksum, final long fileSize, final DocumentType type) {
        return documentRepository.existsByChecksumAndFileSize(checksum, fileSize, type.name()).block();
    }

    public Mono<DocumentEntity> getDocumentEntity(final String documentId) {
        return documentRepository.findById(documentId)
                .map(documentEntityTransformer::transform);
    }

    /**
     * Return the document entities belonging to the provided status.
     *
     * @param documentStatus the status to query for
     * @return the list of documents with the provided values
     */
    public Flux<DocumentEntity> getDocumentEntity(final DocumentStatus documentStatus) {
        return documentRepository.findByStatus(documentStatus.toString())
                .map(documentEntityTransformer::transform);
    }

    /**
     * Return all documents available in the database.
     *
     * @return all documents available in the database
     */
    public Flux<DocumentEntity> getDocumentEntities() {
        return documentRepository.findAll()
                .map(documentEntityTransformer::transform);
    }

    /**
     * Creates a new document. The document is persisted to the database.
     *
     * @param documentCreationContext the parameters of the document to create
     * @return the freshly created document
     */
    public Mono<DocumentEntity> newDocumentEntity(final DocumentCreationContext documentCreationContext) {
        final DocumentDatabaseEntity documentDatabaseEntity = new DocumentDatabaseEntity();

        documentDatabaseEntity.setId(documentCreationContext.getId());
        documentDatabaseEntity.setType(documentCreationContext.getType().toString());
        documentDatabaseEntity.setStatus(documentCreationContext.getStatus().toString());
        documentDatabaseEntity.setChecksum(documentCreationContext.getChecksum());
        documentDatabaseEntity.setFileSize(documentCreationContext.getFileSize());
        documentDatabaseEntity.setDownloaderVersion(documentCreationContext.getVersionNumber());
        documentDatabaseEntity.setCompression(documentCreationContext.getCompression().name());
        documentDatabaseEntity.setDownloadDate(Instant.now());

        return documentRepository.insertDocument(documentDatabaseEntity)
                .map(documentEntityTransformer::transform);
    }
}
