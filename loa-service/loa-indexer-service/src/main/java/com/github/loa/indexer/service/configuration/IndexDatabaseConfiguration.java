package com.github.loa.indexer.service.configuration;

import lombok.RequiredArgsConstructor;
import org.apache.http.HttpHost;
import org.apache.http.client.config.RequestConfig;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestClientBuilder;
import org.elasticsearch.client.RestHighLevelClient;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.TimeUnit;

@Configuration
@RequiredArgsConstructor
@ConditionalOnProperty(name = "loa.indexer.database")
public class IndexDatabaseConfiguration {

    private final IndexDatabaseConfigurationProperties indexDatabaseConfigurationProperties;

    @Bean
    public HttpHost httpHost() {
        return new HttpHost(indexDatabaseConfigurationProperties.getHost(), indexDatabaseConfigurationProperties.getPort(), "http");
    }

    @Bean
    public RestHighLevelClient restHighLevelClient(final HttpHost httpHost) {
        return new RestHighLevelClient(
                RestClient.builder(httpHost)
                        .setRequestConfigCallback(
                                requestConfigBuilder -> requestConfigBuilder
                                        .setConnectTimeout((int) TimeUnit.MINUTES.toMillis(1))
                                        .setSocketTimeout((int) TimeUnit.MINUTES.toMillis(30))
                        )
        );
    }
}
