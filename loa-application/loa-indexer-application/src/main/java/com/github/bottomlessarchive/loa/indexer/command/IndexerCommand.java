package com.github.bottomlessarchive.loa.indexer.command;

import com.github.bottomlessarchive.loa.document.service.domain.DocumentStatus;
import com.github.bottomlessarchive.loa.document.service.entity.factory.DocumentEntityFactory;
import com.github.bottomlessarchive.loa.indexer.DocumentEntityIndexer;
import com.github.bottomlessarchive.loa.indexer.configuration.IndexerConfigurationProperties;
import com.github.bottomlessarchive.loa.indexer.service.search.DocumentSearchClient;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class IndexerCommand implements CommandLineRunner {

    private final DocumentSearchClient documentSearchClient;
    private final DocumentEntityFactory documentEntityFactory;
    private final DocumentEntityIndexer documentEntityIndexer;
    private final IndexerConfigurationProperties indexerConfigurationProperties;

    @Override
    public void run(final String... args) throws InterruptedException {
        if (!documentSearchClient.isSearchEngineInitialized()) {
            log.info("Initializing the search engine!");

            documentSearchClient.initializeSearchEngine();
        }

        log.info("Start document indexing.");

        while(true) {
            documentEntityFactory.getDocumentEntity(DocumentStatus.DOWNLOADED, indexerConfigurationProperties.batchSize())
                    .forEach(documentEntityIndexer::processDocumentEntity);

            log.info("Finished indexing a batch of documents. Waiting five second before looking for new ones.");

            Thread.sleep(5000);
        }
    }

}
