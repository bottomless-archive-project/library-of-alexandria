package com.github.loa.administrator.command.indexer;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.client.indices.CreateIndexRequest;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.xcontent.XContentType;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.file.Files;

/**
 * This command will initialize the indexer database.
 */
@Slf4j
@Component
@RequiredArgsConstructor
@ConditionalOnProperty("initialize-indexer")
public class IndexerInitializerCommand implements CommandLineRunner {

    private final RestHighLevelClient restHighLevelClient;
    private final ResourceLoader resourceLoader;

    /**
     * Runs the command.
     *
     * @param args the command arguments, none yet
     */
    @Override
    public void run(final String... args) throws IOException {
        initializeIndex();
    }

    private void initializeIndex() throws IOException {
        log.info("Started initializing the search database!");

        final String mappingConfiguration = loadMappingConfiguration();

        final CreateIndexRequest createIndexRequest = new CreateIndexRequest("vault_documents")
                .mapping(mappingConfiguration, XContentType.JSON)
                .settings(
                        Settings.builder()
                                .put("index.number_of_shards", 10)
                                .put("index.codec", "best_compression")
                                .put("index.refresh_interval", "60s")
                );

        restHighLevelClient.indices().create(createIndexRequest, RequestOptions.DEFAULT);

        log.info("Finished initializing the search database!");
    }

    private String loadMappingConfiguration() throws IOException {
        return new String(Files.readAllBytes(
                resourceLoader.getResource("classpath:indexer/mapping.json").getFile().toPath()));
    }
}
