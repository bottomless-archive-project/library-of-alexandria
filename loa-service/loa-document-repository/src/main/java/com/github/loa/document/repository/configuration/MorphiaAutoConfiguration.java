package com.github.loa.document.repository.configuration;

import com.github.loa.document.repository.domain.DocumentDatabaseEntity;
import com.mongodb.MongoClient;
import com.mongodb.MongoClientOptions;
import com.mongodb.MongoCredential;
import com.mongodb.ServerAddress;
import dev.morphia.Datastore;
import dev.morphia.Morphia;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.StringUtils;

@Configuration
@RequiredArgsConstructor
public class MorphiaAutoConfiguration {

    private static final String LOA_DATABASE_NAME = "loa";

    private final DatabaseConfigurationProperties databaseConfigurationProperties;

    @Bean
    public Datastore datastore(final Morphia morphia, final MongoClient mongoClient) {
        return morphia.createDatastore(mongoClient, LOA_DATABASE_NAME);
    }

    @Bean
    public Morphia morphia() {
        final Morphia morphia = new Morphia();

        morphia.map(DocumentDatabaseEntity.class);

        return morphia;
    }

    @Bean
    public MongoClient mongoClient(final ServerAddress serverAddress, final MongoCredential mongoCredential) {
        if (StringUtils.isEmpty(databaseConfigurationProperties.getUsername())) {
            return new MongoClient(serverAddress);
        } else {
            return new MongoClient(serverAddress, mongoCredential, MongoClientOptions.builder().build());
        }
    }

    @Bean
    public ServerAddress serverAddress() {
        return new ServerAddress(databaseConfigurationProperties.getHost(),
                databaseConfigurationProperties.getPort());
    }

    @Bean
    protected MongoCredential mongoCredential() {
        return MongoCredential.createCredential(databaseConfigurationProperties.getUsername(), LOA_DATABASE_NAME,
                databaseConfigurationProperties.getPassword().toCharArray());
    }
}
