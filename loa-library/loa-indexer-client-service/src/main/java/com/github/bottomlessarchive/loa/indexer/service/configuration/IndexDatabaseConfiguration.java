package com.github.bottomlessarchive.loa.indexer.service.configuration;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.json.jackson.JacksonJsonpMapper;
import co.elastic.clients.transport.rest_client.RestClientTransport;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.HttpHost;
import org.elasticsearch.client.RestClient;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.TimeUnit;

@Slf4j
@Configuration
@RequiredArgsConstructor
@ConditionalOnProperty(value = "loa.indexer.database.enabled", havingValue = "true", matchIfMissing = true)
public class IndexDatabaseConfiguration {

    private final IndexDatabaseConfigurationProperties indexDatabaseConfigurationProperties;

    @Bean
    public ElasticsearchClient elasticsearchClient(final RestClientTransport restClientTransport) {
        return new ElasticsearchClient(restClientTransport);
    }

    @Bean
    public RestClientTransport restClientTransport(final RestClient restClient) {
        return new RestClientTransport(restClient, new JacksonJsonpMapper());
    }

    @Bean
    public RestClient restClient(final HttpHost httpHost) {
        return RestClient.builder(httpHost)
                .setRequestConfigCallback(
                        requestConfigBuilder -> requestConfigBuilder
                                .setConnectTimeout((int) TimeUnit.MINUTES.toMillis(1))
                                .setSocketTimeout((int) TimeUnit.MINUTES.toMillis(30))
                                .setConnectionRequestTimeout((int) TimeUnit.MINUTES.toMillis(30))
                )
                .build();
    }

    @Bean
    public HttpHost httpHost() {
        if (log.isInfoEnabled()) {
            log.info("Connecting to ElasticSearch on host: {} port: {}!", indexDatabaseConfigurationProperties.host(),
                    indexDatabaseConfigurationProperties.port());
        }

        return new HttpHost(indexDatabaseConfigurationProperties.host(), indexDatabaseConfigurationProperties.port());
    }
}
