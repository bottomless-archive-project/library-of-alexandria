package com.github.bottomlessarchive.loa.indexer;

import com.github.bottomlessarchive.loa.document.service.DocumentManipulator;
import com.github.bottomlessarchive.loa.document.service.domain.DocumentEntity;
import com.github.bottomlessarchive.loa.indexer.service.indexer.IndexerClient;
import com.github.bottomlessarchive.loa.indexer.service.indexer.domain.IndexingContext;
import com.github.bottomlessarchive.loa.parser.domain.ParsingResult;
import com.github.bottomlessarchive.loa.parser.service.DocumentDataParser;
import com.github.bottomlessarchive.loa.vault.client.service.VaultClientService;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.io.InputStream;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Semaphore;

@Slf4j
@Service
@RequiredArgsConstructor
public class DocumentEntityIndexer {

    private final IndexerClient indexerClient;
    private final VaultClientService vaultClientService;
    private final DocumentDataParser documentDataParser;
    private final DocumentManipulator documentManipulator;
    @Qualifier("indexerSemaphore")
    private final Semaphore indexerSemaphore;
    @Qualifier("indexerExecutorService")
    private final ExecutorService indexerExecutorService;

    @SneakyThrows
    public void processDocumentEntity(final DocumentEntity documentEntity) {
        indexerSemaphore.acquire();

        indexerExecutorService.execute(() -> doProcessDocumentEntity(documentEntity));
    }

    private void doProcessDocumentEntity(final DocumentEntity documentEntity) {
        try {
            processDocument(documentEntity);
        } catch (Exception e) {
            log.warn("Failed to index document {}! Cause: '{}'.", documentEntity.getId(), e.getMessage());

            documentManipulator.markCorrupt(documentEntity.getId());
        }

        indexerSemaphore.release();
    }

    @SneakyThrows
    private void processDocument(final DocumentEntity documentEntity) {
        try (InputStream documentContent = vaultClientService.queryDocument(documentEntity)) {
            final ParsingResult documentMetadata = documentDataParser.parseDocumentMetadata(documentEntity.getId(),
                    documentEntity.getType(), documentContent);

            indexerClient.indexDocument(
                    IndexingContext.builder()
                            .id(documentMetadata.getId())
                            .author(documentMetadata.getAuthor())
                            .content(documentMetadata.getContent())
                            .date(documentMetadata.getDate())
                            .language(documentMetadata.getLanguage())
                            .pageCount(documentMetadata.getPageCount())
                            .title(documentMetadata.getTitle())
                            .type(documentMetadata.getType())
                            .build()
            );
        }
    }
}
