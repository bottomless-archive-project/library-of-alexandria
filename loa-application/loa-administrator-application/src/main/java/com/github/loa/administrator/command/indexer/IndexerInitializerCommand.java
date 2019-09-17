package com.github.loa.administrator.command.indexer;

import lombok.RequiredArgsConstructor;
import org.elasticsearch.action.ingest.PutPipelineRequest;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.client.indices.CreateIndexRequest;
import org.elasticsearch.common.bytes.BytesArray;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.xcontent.XContentType;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

/**
 * This command will initialize the indexer database.
 */
@Component
@RequiredArgsConstructor
@ConditionalOnProperty("initialize-indexer")
public class IndexerInitializerCommand implements CommandLineRunner {

    private final RestHighLevelClient restHighLevelClient;

    /**
     * Runs the command.
     *
     * @param args the command arguments, none yet
     */
    @Override
    public void run(final String... args) throws IOException {
        initializeAttachmentPipeline();
        initializeIndex();
    }

    private void initializeAttachmentPipeline() throws IOException {
        final String source = "{\"description\":\"Processors for the Library of Alexandria project.\", " +
                "\"processors\":[{\"attachment\":{\"field\":\"content\", \"indexed_chars\" : -1}, \"remove\": {" +
                "\"field\": \"content\"}}]}";

        final PutPipelineRequest request = new PutPipelineRequest("vault-document-pipeline",
                new BytesArray(source.getBytes(StandardCharsets.UTF_8)), XContentType.JSON
        );

        restHighLevelClient.ingest().putPipeline(request, RequestOptions.DEFAULT);
    }

    private void initializeIndex() throws IOException {
        final CreateIndexRequest createIndexRequest = new CreateIndexRequest("vault_documents")
                .settings(
                        Settings.builder()
                                .put("index.number_of_shards", 3)
                                .put("index.codec", "best_compression")
                );

        restHighLevelClient.indices().create(createIndexRequest, RequestOptions.DEFAULT);
    }
}
