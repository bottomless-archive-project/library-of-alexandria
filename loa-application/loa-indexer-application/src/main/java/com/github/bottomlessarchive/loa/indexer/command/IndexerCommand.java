package com.github.bottomlessarchive.loa.indexer.command;

import com.github.bottomlessarchive.loa.indexer.service.indexer.domain.IndexingContext;
import com.github.bottomlessarchive.loa.parser.domain.ParsingResult;
import com.github.bottomlessarchive.loa.parser.service.DocumentDataParser;
import com.github.bottomlessarchive.loa.document.service.domain.DocumentEntity;
import com.github.bottomlessarchive.loa.document.service.domain.DocumentStatus;
import com.github.bottomlessarchive.loa.document.service.entity.factory.DocumentEntityFactory;
import com.github.bottomlessarchive.loa.indexer.service.indexer.IndexerClient;
import com.github.bottomlessarchive.loa.indexer.service.search.DocumentSearchClient;
import com.github.bottomlessarchive.loa.vault.client.service.VaultClientService;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.io.InputStream;

@Slf4j
@Component
@RequiredArgsConstructor
public class IndexerCommand implements CommandLineRunner {

    private final IndexerClient indexerClient;
    private final VaultClientService vaultClientService;
    private final DocumentDataParser documentDataParser;
    private final DocumentSearchClient documentSearchClient;
    private final DocumentEntityFactory documentEntityFactory;

    @Override
    public void run(final String... args) {
        if (!documentSearchClient.isSearchEngineInitialized()) {
            log.info("Initializing the search engine!");

            documentSearchClient.initializeSearchEngine();
        }

        log.info("Start document indexing.");

        documentEntityFactory.getDocumentEntity(DocumentStatus.DOWNLOADED)
                .forEach(this::processDocument);
    }

    @SneakyThrows
    private void processDocument(final DocumentEntity documentEntity) {
        try (final InputStream documentContent = vaultClientService.queryDocument(documentEntity)) {
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
