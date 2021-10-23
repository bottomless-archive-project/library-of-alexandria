package com.github.bottomlessarchive.loa.repository.configuration;

import com.mongodb.ConnectionString;
import com.mongodb.MongoClientSettings;
import com.mongodb.ServerAddress;
import com.mongodb.reactivestreams.client.MongoClient;
import com.mongodb.reactivestreams.client.MongoClients;
import com.mongodb.reactivestreams.client.MongoDatabase;
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

    private final RepositoryConfigurationProperties repositoryConfigurationProperties;

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
        final MongoClientSettings.Builder builder = MongoClientSettings.builder()
                .codecRegistry(codecRegistry)
                .uuidRepresentation(UuidRepresentation.JAVA_LEGACY);

        if (repositoryConfigurationProperties.isUriConfiguration()) {
            builder.applyConnectionString(new ConnectionString(repositoryConfigurationProperties.getUri()));
        } else {
            builder.applyToClusterSettings(clusterBuilder ->
                    clusterBuilder.hosts(
                            List.of(
                                    new ServerAddress(
                                            repositoryConfigurationProperties.getHost(),
                                            repositoryConfigurationProperties.getPort()
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
