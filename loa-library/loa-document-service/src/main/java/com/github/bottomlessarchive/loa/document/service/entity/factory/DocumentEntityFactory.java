package com.github.bottomlessarchive.loa.document.service.entity.factory;

import com.github.bottomlessarchive.loa.document.repository.DocumentRepository;
import com.github.bottomlessarchive.loa.document.service.entity.factory.domain.DocumentCreationContext;
import com.github.bottomlessarchive.loa.repository.service.HexConverter;
import com.github.bottomlessarchive.loa.document.repository.domain.DocumentDatabaseEntity;
import com.github.bottomlessarchive.loa.document.service.domain.DocumentEntity;
import com.github.bottomlessarchive.loa.document.service.domain.DocumentStatus;
import com.github.bottomlessarchive.loa.document.service.domain.DocumentType;
import com.github.bottomlessarchive.loa.document.service.entity.transformer.DocumentEntityTransformer;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Instant;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class DocumentEntityFactory {

    private final HexConverter hexConverter;
    private final DocumentRepository documentRepository;
    private final DocumentEntityTransformer documentEntityTransformer;

    /**
     * Return a document by its id.
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
     * Remove a document by its id.
     *
     * @param documentEntity the document to remove
     * @return the result of the removal
     */
    public Mono<Void> removeDocumentEntity(final DocumentEntity documentEntity) {
        return documentRepository.removeDocument(documentEntity.getId());
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
     * Return the number of documents available in the database.
     *
     * @return the count of documents available in the database
     */
    public Mono<Long> getDocumentCount() {
        return documentRepository.count();
    }

    /**
     * Return the approximate number of documents available in the database. It is much faster than
     * {@link #getDocumentCount()} but not accurate.
     *
     * @return the approximate count of documents available in the database
     */
    public Mono<Long> getEstimatedDocumentCount() {
        return documentRepository.estimateCount();
    }

    /**
     * Return the number of documents available in the database grouped by status.
     *
     * @return the count of documents grouped by status
     */
    public Mono<Map<DocumentStatus, Integer>> getCountByStatus() {
        return documentRepository.countByStatus()
                .map(result -> result.entrySet().stream()
                        .collect(Collectors.toMap(entry ->
                                DocumentStatus.valueOf(entry.getKey()), Map.Entry::getValue))
                );
    }

    /**
     * Return the number of documents available in the database grouped by type.
     *
     * @return the count of documents grouped by type
     */
    public Mono<Map<DocumentType, Integer>> getCountByType() {
        return documentRepository.countByType()
                .map(result -> result.entrySet().stream()
                        .collect(Collectors.toMap(entry ->
                                DocumentType.valueOf(entry.getKey()), Map.Entry::getValue))
                );
    }

    /**
     * Update the status field of all the documents available in the database.
     *
     * @param documentStatus the status to update the documents to
     * @return the result of the update
     */
    public Mono<Void> updateStatus(final DocumentStatus documentStatus) {
        return documentRepository.updateStatus(documentStatus.name());
    }

    public Mono<Void> addSourceLocation(final UUID documentId, final UUID documentLocationId) {
        return documentRepository.addSourceLocation(documentId, documentLocationId);
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
        documentDatabaseEntity.setSourceLocations(
                Set.of(UUID.fromString(documentCreationContext.getSourceLocationId()))
        );

        return documentRepository.insertDocument(documentDatabaseEntity)
                .map(documentEntityTransformer::transform);
    }
}
