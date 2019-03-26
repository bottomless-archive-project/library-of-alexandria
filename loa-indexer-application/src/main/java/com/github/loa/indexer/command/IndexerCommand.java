package com.github.loa.indexer.command;

import com.github.loa.document.service.domain.DocumentEntity;
import com.github.loa.document.service.domain.DocumentStatus;
import com.github.loa.document.service.entity.factory.DocumentEntityFactory;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.HttpHost;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestHighLevelClient;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class IndexerCommand implements CommandLineRunner {

    private final DocumentEntityFactory documentEntityFactory;

    @Override
    public void run(final String... args) {
        log.info("Initializing document indexing.");


        try (RestHighLevelClient client = new RestHighLevelClient(RestClient.builder(
                new HttpHost("localhost", 9200, "http")))) {
            while (true) {
                final List<DocumentEntity> documentEntities = documentEntityFactory
                        .getDocumentEntity(DocumentStatus.DOWNLOADED);

                if (documentEntities.isEmpty()) {
                    log.info("Waiting for a while because no documents are available for indexing.");

                    //TODO: Make this configurable.
                    Thread.sleep(60000);
                } else {
                    documentEntities.forEach(documentEntity -> {
                        //TODO: send for indexing here
                    });
                }
            }
        } catch (IOException | InterruptedException e) {
            log.error("Failed to index documents!", e);
        }

        //...
    }
}
