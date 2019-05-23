package com.github.loa.document.service.entity.factory;

import com.github.loa.document.repository.DocumentRepository;
import com.github.loa.document.repository.domain.DocumentDatabaseEntity;
import com.github.loa.document.service.domain.DocumentEntity;
import com.github.loa.document.service.domain.DocumentStatus;
import com.github.loa.document.service.domain.DocumentType;
import com.github.loa.document.service.entity.factory.domain.DocumentCreationContext;
import com.github.loa.document.service.entity.transformer.DocumentEntityTransformer;
import dev.morphia.query.internal.MorphiaCursor;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

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
        return !documentRepository.findByChecksumAndFileSize(checksum, fileSize, type.name()).isEmpty();
    }

    public Optional<DocumentEntity> getDocumentEntity(final String documentId) {
        final DocumentDatabaseEntity documentDatabaseEntities = documentRepository.findById(documentId);

        return documentDatabaseEntities == null ? Optional.empty() : Optional.of(
                documentEntityTransformer.transform(documentDatabaseEntities));

    }

    /**
     * Return the document entities belonging to the provided status. The returned list size is limited ti 100.
     *
     * @param documentStatus the status to query for
     * @return the list of documents with the provided values
     */
    public List<DocumentEntity> getDocumentEntity(final DocumentStatus documentStatus) {
        final List<DocumentDatabaseEntity> documentDatabaseEntities =
                documentRepository.findByStatus(documentStatus.toString());

        return documentEntityTransformer.transform(documentDatabaseEntities);
    }

    public Stream<DocumentEntity> getDocumentEntities() {
        final MorphiaCursor<DocumentDatabaseEntity> documentDatabaseEntities = documentRepository.findAll();

        return StreamSupport.stream(
                Spliterators.spliteratorUnknownSize(documentDatabaseEntities, Spliterator.ORDERED), false)
                .map(documentEntityTransformer::transform);
    }

    /**
     * Creates a new document. The document is persisted to the database.
     *
     * @param documentCreationContext the parameters of the document to create
     * @return the freshly created document
     */
    public DocumentEntity newDocumentEntity(final DocumentCreationContext documentCreationContext) {
        final DocumentDatabaseEntity documentDatabaseEntity = new DocumentDatabaseEntity();

        documentDatabaseEntity.setId(documentCreationContext.getId());
        documentDatabaseEntity.setType(documentCreationContext.getType().toString());
        documentDatabaseEntity.setStatus(documentCreationContext.getStatus().toString());
        documentDatabaseEntity.setChecksum(documentCreationContext.getChecksum());
        documentDatabaseEntity.setFileSize(documentCreationContext.getFileSize());
        documentDatabaseEntity.setDownloaderVersion(documentCreationContext.getVersionNumber());
        documentDatabaseEntity.setCompression(documentCreationContext.getCompression().name());
        documentDatabaseEntity.setDownloadDate(Instant.now());

        documentRepository.insertDocument(documentDatabaseEntity);

        return getDocumentEntity(documentCreationContext.getId()).get();
    }
}
