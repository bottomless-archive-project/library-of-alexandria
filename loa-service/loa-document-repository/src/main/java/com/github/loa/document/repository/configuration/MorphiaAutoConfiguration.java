package com.github.loa.document.repository.configuration;

import com.github.loa.document.repository.domain.DocumentDatabaseEntity;
import com.github.loa.document.repository.domain.DocumentLocationDatabaseEntity;
import com.mongodb.MongoClient;
import com.mongodb.MongoClientOptions;
import com.mongodb.MongoCredential;
import com.mongodb.ServerAddress;
import dev.morphia.Datastore;
import dev.morphia.Morphia;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Slf4j
@Configuration
@RequiredArgsConstructor
public class MorphiaAutoConfiguration {

    private static final String LOA_DATABASE_NAME = "loa";

    private final DatabaseConfigurationProperties databaseConfigurationProperties;

    @Bean
    public Datastore datastore(final Morphia morphia, final MongoClient mongoClient) {
        final Datastore datastore = morphia.createDatastore(mongoClient, LOA_DATABASE_NAME);

        datastore.ensureIndexes();

        return datastore;
    }

    @Bean
    public Morphia morphia() {
        final Morphia morphia = new Morphia();

        morphia.map(DocumentDatabaseEntity.class);
        morphia.map(DocumentLocationDatabaseEntity.class);

        return morphia;
    }

    @Bean
    @ConditionalOnProperty(value = "loa.database.username", matchIfMissing = true)
    public MongoClient mongoClient(final ServerAddress serverAddress) {
        return new MongoClient(serverAddress);
    }

    @Bean
    @ConditionalOnProperty("loa.database.username")
    public MongoClient mongoClient(final ServerAddress serverAddress, final MongoCredential mongoCredential) {
        return new MongoClient(serverAddress, mongoCredential,
                MongoClientOptions.builder()
                        .connectionsPerHost(20)
                        .minConnectionsPerHost(20)
                        .build()
        );
    }

    @Bean
    public ServerAddress serverAddress() {
        log.info("Setting up mongodb connection to " + databaseConfigurationProperties.getHost() + " on port "
                + databaseConfigurationProperties.getPort() + ".");

        return new ServerAddress(databaseConfigurationProperties.getHost(),
                databaseConfigurationProperties.getPort());
    }

    @Bean
    @ConditionalOnProperty("loa.database.username")
    protected MongoCredential mongoCredential() {
        return MongoCredential.createCredential(databaseConfigurationProperties.getUsername(), LOA_DATABASE_NAME,
                databaseConfigurationProperties.getPassword().toCharArray());
    }
}
