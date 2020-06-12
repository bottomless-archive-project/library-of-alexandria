package com.github.loa.document.service.entity.factory;

import com.github.loa.document.repository.DocumentRepository;
import com.github.loa.document.repository.domain.DocumentDatabaseEntity;
import com.github.loa.document.service.domain.DocumentEntity;
import com.github.loa.document.service.domain.DocumentStatus;
import com.github.loa.document.service.entity.factory.domain.DocumentCreationContext;
import com.github.loa.document.service.entity.transformer.DocumentEntityTransformer;
import com.github.loa.repository.service.HexConverter;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Instant;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class DocumentEntityFactory {

    private final HexConverter hexConverter;
    private final DocumentRepository documentRepository;
    private final DocumentEntityTransformer documentEntityTransformer;

    /**
     * Return a document by it's id.
     *
     * @param documentId the id of the document
     * @return the document that belongs to the provided id
     */
    public Mono<DocumentEntity> getDocumentEntity(final UUID documentId) {
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

    public Mono<Long> getDocumentCount() {
        return documentRepository.count();
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
        documentDatabaseEntity.setVault(documentCreationContext.getVault());
        documentDatabaseEntity.setType(documentCreationContext.getType().toString());
        documentDatabaseEntity.setStatus(documentCreationContext.getStatus().toString());
        documentDatabaseEntity.setChecksum(hexConverter.decode(documentCreationContext.getChecksum()));
        documentDatabaseEntity.setFileSize(documentCreationContext.getFileSize());
        documentDatabaseEntity.setDownloaderVersion(documentCreationContext.getVersionNumber());
        documentDatabaseEntity.setCompression(documentCreationContext.getCompression().name());
        documentDatabaseEntity.setDownloadDate(Instant.now());
        documentDatabaseEntity.setSource(documentCreationContext.getSource());

        return documentRepository.insertDocument(documentDatabaseEntity)
                .map(documentEntityTransformer::transform);
    }
}
