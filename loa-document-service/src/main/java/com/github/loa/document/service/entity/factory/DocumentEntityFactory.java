package com.github.loa.document.service.entity.factory;

import com.github.loa.compression.domain.DocumentCompression;
import com.github.loa.document.repository.DocumentRepository;
import com.github.loa.document.service.domain.DocumentEntity;
import com.github.loa.document.service.entity.transformer.DocumentEntityTransformer;
import com.github.loa.document.service.domain.DocumentStatus;
import com.github.loa.document.service.entity.factory.domain.DocumentCreationContext;
import com.github.loa.document.repository.domain.DocumentDatabaseEntity;
import lombok.RequiredArgsConstructor;
import org.apache.ibatis.cursor.Cursor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

@Service
@RequiredArgsConstructor
public class DocumentEntityFactory {

    private final DocumentRepository documentRepository;
    private final DocumentEntityTransformer documentEntityTransformer;

    /**
     * Check if a document exists based on the provided document id.
     *
     * @param documentId the id of the document to check
     * @return return true if a document exists with the provided id
     */
    public boolean isDocumentExists(final String documentId) {
        return documentRepository.findById(documentId) != null;
    }

    /**
     * Return true if any document exists with the provided checksum and file size.
     *
     * @param checksum the checksum to use for the checking
     * @param fileSize the file size used for the checking
     * @return return true if a document exist with the provided parameters or false otherwise
     */
    public boolean isDocumentExists(final String checksum, final long fileSize) {
        return !documentRepository.findByChecksumAndFileSize(checksum, fileSize).isEmpty();
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

    public List<DocumentEntity> getDocumentEntity(final DocumentCompression compression) {
        final List<DocumentDatabaseEntity> documentDatabaseEntities =
                documentRepository.findByCompression(compression.toString());

        return documentEntityTransformer.transform(documentDatabaseEntities);
    }

    /**
     * Return the document entities belonging to the provided checksum and file size values.
     *
     * @param checksum the checksum value of the document
     * @param fileSize the file size value of the document
     * @return the list of documents with the provided values
     */
    public List<DocumentEntity> getDocumentEntity(final String checksum, final long fileSize) {
        final List<DocumentDatabaseEntity> documentDatabaseEntities =
                documentRepository.findByChecksumAndFileSize(checksum, fileSize);

        return documentEntityTransformer.transform(documentDatabaseEntities);
    }

    public Stream<DocumentEntity> getDocumentEntities() {
        final Cursor<DocumentDatabaseEntity> documentDatabaseEntities = documentRepository.findAll();

        return StreamSupport.stream(documentDatabaseEntities.spliterator(), false)
                .map(documentEntityTransformer::transform);
    }

    /**
     * Creates a new document. The document is persisted to the database.
     *
     * @param documentCreationContext the parameters of the document to create
     */
    public void newDocumentEntity(final DocumentCreationContext documentCreationContext) {
        final DocumentDatabaseEntity documentDatabaseEntity = new DocumentDatabaseEntity();

        documentDatabaseEntity.setId(documentCreationContext.getId());
        documentDatabaseEntity.setUrl(documentCreationContext.getLocation().toString());
        documentDatabaseEntity.setStatus(documentCreationContext.getStatus().toString());
        documentDatabaseEntity.setChecksum(documentCreationContext.getChecksum());
        documentDatabaseEntity.setFileSize(documentCreationContext.getFileSize());
        documentDatabaseEntity.setDownloaderVersion(documentCreationContext.getVersionNumber());
        documentDatabaseEntity.setSource(documentCreationContext.getSource());
        documentDatabaseEntity.setCompression(documentCreationContext.getCompression().name());

        documentRepository.insertDocument(documentDatabaseEntity);
    }
}
