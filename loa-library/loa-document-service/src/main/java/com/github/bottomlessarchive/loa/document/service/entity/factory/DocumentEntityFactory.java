package com.github.bottomlessarchive.loa.document.service.entity.factory;

import com.github.bottomlessarchive.loa.document.repository.DocumentRepository;
import com.github.bottomlessarchive.loa.document.service.domain.DuplicateDocumentException;
import com.github.bottomlessarchive.loa.document.service.entity.factory.domain.DocumentCreationContext;
import com.github.bottomlessarchive.loa.number.service.HexConverter;
import com.github.bottomlessarchive.loa.document.repository.domain.DocumentDatabaseEntity;
import com.github.bottomlessarchive.loa.document.service.domain.DocumentEntity;
import com.github.bottomlessarchive.loa.document.service.domain.DocumentStatus;
import com.github.bottomlessarchive.loa.document.service.entity.transformer.DocumentEntityTransformer;
import com.github.bottomlessarchive.loa.type.domain.DocumentType;
import com.mongodb.MongoWriteException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Collections;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

@Service
@RequiredArgsConstructor
public class DocumentEntityFactory {

    private static final int DUPLICATE_DOCUMENT_ID_ERROR_CODE = 11000;

    private final HexConverter hexConverter;
    private final DocumentRepository documentRepository;
    private final DocumentEntityTransformer documentEntityTransformer;

    /**
     * Return a document by its id.
     *
     * @param documentId the id of the document
     * @return the document that belongs to the provided id
     */
    public Optional<DocumentEntity> getDocumentEntity(final UUID documentId) {
        return documentRepository.findById(documentId)
                .map(documentEntityTransformer::transform);
    }

    public Optional<DocumentEntity> getDocumentEntity(final String checksum, final long fileSize, final String type) {
        final byte[] checksumAsHex = hexConverter.decode(checksum);

        return documentRepository.findByChecksumAndFileSizeAndType(checksumAsHex, fileSize, type)
                .map(documentEntityTransformer::transform);
    }

    /**
     * Return the document entities belonging to the provided status.
     *
     * @param documentStatus the status to query for
     * @return the list of documents with the provided values
     */
    public Stream<DocumentEntity> getDocumentEntity(final DocumentStatus documentStatus, final int batchSize) {
        return StreamSupport.stream(documentRepository.findByStatus(documentStatus.toString(), batchSize).spliterator(), false)
                .map(documentEntityTransformer::transform);
    }

    /**
     * Remove a document by its id.
     *
     * @param documentEntity the document to remove
     */
    public void removeDocumentEntity(final DocumentEntity documentEntity) {
        documentRepository.removeDocument(documentEntity.getId());
    }

    /**
     * Return all documents available in the database.
     *
     * @return all documents available in the database
     */
    public Stream<DocumentEntity> getDocumentEntitiesSync() {
        return StreamSupport.stream(documentRepository.findAll().spliterator(), false)
                .map(documentEntityTransformer::transform);
    }

    /**
     * Return the approximate number of documents available in the database.
     *
     * @return the approximate count of documents available in the database
     */
    public long getEstimatedDocumentCount() {
        return documentRepository.estimateCount();
    }

    /**
     * Return the number of documents available in the database grouped by status.
     *
     * @return the map of the document count grouped by status
     */
    public Map<DocumentStatus, Integer> getCountByStatus() {
        return documentRepository.countByStatus().entrySet().stream()
                .collect(Collectors.toMap(entry -> DocumentStatus.valueOf(entry.getKey()), Map.Entry::getValue));
    }

    /**
     * Return the number of documents available in the database grouped by type.
     *
     * @return the map of the document count grouped by type
     */
    public Map<DocumentType, Integer> getCountByType() {
        return documentRepository.countByType().entrySet().stream()
                .collect(Collectors.toMap(entry -> DocumentType.valueOf(entry.getKey()), Map.Entry::getValue));
    }

    /**
     * Update the status field of all the documents available in the database.
     *
     * @param documentStatus the status to update the documents to
     */
    public void updateStatus(final DocumentStatus documentStatus) {
        documentRepository.updateStatus(documentStatus.name());
    }

    public void addSourceLocation(final UUID documentId, final String documentLocationId) {
        documentRepository.addSourceLocation(documentId, documentLocationId);
    }

    /**
     * Creates a new document. The document is persisted to the database.
     *
     * @param documentCreationContext the parameters of the document to create
     * @return the freshly created document
     */
    public DocumentEntity newDocumentEntity(final DocumentCreationContext documentCreationContext) {
        final Set<byte[]> sourceLocations = documentCreationContext.sourceLocationId()
                .map(value -> Set.of(hexConverter.decode(value)))
                .orElse(Collections.emptySet());

        final DocumentDatabaseEntity documentDatabaseEntity = DocumentDatabaseEntity.builder()
                .id(documentCreationContext.id())
                .vault(documentCreationContext.vault())
                .type(documentCreationContext.type().toString())
                .status(documentCreationContext.status().toString())
                .compression(documentCreationContext.compression().name())
                .source(documentCreationContext.source())
                .beacon(documentCreationContext.beacon())
                .sourceLocations(sourceLocations)
                .checksum(hexConverter.decode(documentCreationContext.checksum()))
                .fileSize(documentCreationContext.fileSize())
                .downloaderVersion(documentCreationContext.versionNumber())
                .downloadDate(Instant.now())
                .build();

        try {
            documentRepository.insertDocument(documentDatabaseEntity);
        } catch (final MongoWriteException e) {
            if (e.getError().getCode() == DUPLICATE_DOCUMENT_ID_ERROR_CODE) {
                throw new DuplicateDocumentException("Document with id: " + documentDatabaseEntity.id() + " and checksum: "
                        + new String(documentDatabaseEntity.checksum()) + " is a duplicate!", e);
            }

            throw e;
        }

        return documentEntityTransformer.transform(documentDatabaseEntity);
    }
}
