package com.github.loa.indexer.service.configuration;

import lombok.RequiredArgsConstructor;
import org.apache.http.HttpHost;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestHighLevelClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@RequiredArgsConstructor
public class DatabaseConfiguration {

    private final DatabaseConfigurationProperties databaseConfigurationProperties;

    @Bean
    public HttpHost httpHost() {
        return new HttpHost(databaseConfigurationProperties.getHost(), databaseConfigurationProperties.getPort(), "http");
    }

    @Bean
    public RestHighLevelClient restHighLevelClient(final HttpHost httpHost) {
        return new RestHighLevelClient(RestClient.builder(httpHost));
    }
}
