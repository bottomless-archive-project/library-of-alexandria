package com.github.bottomlessarchive.loa.indexer.service.configuration;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.HttpHost;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestHighLevelClient;
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
    public HttpHost httpHost() {
        if (log.isInfoEnabled()) {
            log.info("Connecting to ElasticSearch on host: {} port: {}!", indexDatabaseConfigurationProperties.getHost(),
                    indexDatabaseConfigurationProperties.getPort());
        }

        return new HttpHost(indexDatabaseConfigurationProperties.getHost(), indexDatabaseConfigurationProperties.getPort());
    }

    @Bean
    public RestHighLevelClient restHighLevelClient(final HttpHost httpHost) {
        return new RestHighLevelClient(
                RestClient.builder(httpHost)
                        .setRequestConfigCallback(
                                requestConfigBuilder -> requestConfigBuilder
                                        .setConnectTimeout((int) TimeUnit.MINUTES.toMillis(1))
                                        .setSocketTimeout((int) TimeUnit.MINUTES.toMillis(30))
                                        .setConnectionRequestTimeout((int) TimeUnit.MINUTES.toMillis(30))
                        )
        );
    }
}
