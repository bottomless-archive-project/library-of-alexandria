package com.github.loa.indexer.command;

import com.github.loa.document.service.domain.DocumentEntity;
import com.github.loa.document.service.domain.DocumentStatus;
import com.github.loa.document.service.entity.factory.DocumentEntityFactory;
import com.github.loa.indexer.service.IndexRequestFactory;
import com.github.loa.indexer.service.response.IndexResponseActionListenerFactory;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.HttpHost;
import org.elasticsearch.action.ActionListener;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.client.RequestOptions;
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
    private final IndexRequestFactory indexRequestFactory;
    private final IndexResponseActionListenerFactory indexResponseActionListenerFactory;

    @Override
    public void run(final String... args) {
        log.info("Initializing document indexing.");

        //TODO: Make these values configurable
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
                        final IndexRequest indexRequest = indexRequestFactory.newIndexRequest(documentEntity);

                        client.indexAsync(indexRequest, RequestOptions.DEFAULT,
                                indexResponseActionListenerFactory.newListener());

                    });
                }
            }
        } catch (IOException | InterruptedException e) {
            log.error("Failed to index documents!", e);
        }
    }
}
