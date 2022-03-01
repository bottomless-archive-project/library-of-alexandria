package com.github.bottomlessarchive.loa.repository.configuration;

import com.github.bottomlessarchive.loa.application.domain.ApplicationType;
import com.github.bottomlessarchive.loa.conductor.service.client.ConductorClient;
import com.github.bottomlessarchive.loa.conductor.service.domain.ServiceInstanceEntity;
import com.mongodb.ConnectionString;
import com.mongodb.MongoClientSettings;
import com.mongodb.ServerAddress;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoDatabase;
import lombok.RequiredArgsConstructor;
import org.bson.UuidRepresentation;
import org.bson.codecs.configuration.CodecRegistry;
import org.bson.codecs.pojo.PojoCodecProvider;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

import static org.bson.codecs.configuration.CodecRegistries.fromProviders;
import static org.bson.codecs.configuration.CodecRegistries.fromRegistries;

@Configuration
@RequiredArgsConstructor
public class RepositoryConfiguration {

    private final ConductorClient conductorClient;

    @Bean
    public MongoDatabase mongoDatabase(final MongoClient mongoClient) {
        return mongoClient.getDatabase("loa");
    }

    @Bean
    public MongoClient mongoClient(final MongoClientSettings mongoClientSettings) {
        return MongoClients.create(mongoClientSettings);
    }

    @Bean
    public MongoClientSettings mongoClientSettings(final CodecRegistry codecRegistry) {
        final ServiceInstanceEntity serviceInstanceEntity = conductorClient.getInstance(ApplicationType.DOCUMENT_DATABASE)
                .orElseThrow(() -> new IllegalStateException("Document database (MongoDB) server is not available!"));

        final MongoClientSettings.Builder builder = MongoClientSettings.builder()
                .codecRegistry(codecRegistry)
                .uuidRepresentation(UuidRepresentation.JAVA_LEGACY);

        if (serviceInstanceEntity.getProperty("uri").isPresent()) {
            builder.applyConnectionString(new ConnectionString(serviceInstanceEntity.getProperty("uri").get().getValue()));
        } else {
            builder.applyToClusterSettings(clusterBuilder ->
                    clusterBuilder.hosts(
                            List.of(
                                    new ServerAddress(
                                            serviceInstanceEntity.getLocation(),
                                            serviceInstanceEntity.getPort()
                                    )
                            )
                    )
            );
        }

        return builder.build();
    }

    @Bean
    public CodecRegistry codecRegistry(final PojoCodecProvider pojoCodecProvider) {
        return fromRegistries(MongoClientSettings.getDefaultCodecRegistry(), fromProviders(pojoCodecProvider));
    }

    @Bean
    public PojoCodecProvider pojoCodecProvider() {
        return PojoCodecProvider.builder()
                .automatic(true)
                .build();
    }
}
