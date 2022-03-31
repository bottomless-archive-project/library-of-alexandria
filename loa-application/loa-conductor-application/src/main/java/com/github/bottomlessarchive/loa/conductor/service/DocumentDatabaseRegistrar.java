package com.github.bottomlessarchive.loa.conductor.service;

import com.github.bottomlessarchive.loa.application.domain.ApplicationType;
import com.github.bottomlessarchive.loa.conductor.configuration.IndexDatabaseConfigurationProperties;
import com.github.bottomlessarchive.loa.conductor.configuration.RepositoryConfigurationProperties;
import com.github.bottomlessarchive.loa.conductor.service.domain.ServiceInstanceProperty;
import com.github.bottomlessarchive.loa.conductor.service.domain.ServiceInstanceRegistrationContext;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;

import java.util.LinkedList;
import java.util.List;

@Component
@RequiredArgsConstructor
public class DocumentDatabaseRegistrar implements ApplicationListener<ContextRefreshedEvent> {

    private final ServiceInstanceContainer serviceInstanceContainer;
    private final RepositoryConfigurationProperties repositoryConfigurationProperties;
    private final IndexDatabaseConfigurationProperties indexDatabaseConfigurationProperties;

    @Override
    public void onApplicationEvent(@NonNull final ContextRefreshedEvent event) {
        registerDocumentDatabase();
        registerDocumentIndex();
    }

    private void registerDocumentDatabase() {
        final List<ServiceInstanceProperty> properties = new LinkedList<>();

        if (repositoryConfigurationProperties.uri() != null) {
            properties.add(
                    ServiceInstanceProperty.builder()
                            .name("uri")
                            .value(repositoryConfigurationProperties.uri())
                            .build()
            );
        }

        properties.add(
                ServiceInstanceProperty.builder()
                        .name("noCursorTimeout")
                        .value(String.valueOf(repositoryConfigurationProperties.noCursorTimeout()))
                        .build()
        );

        serviceInstanceContainer.registerServiceInstance(ApplicationType.DOCUMENT_DATABASE,
                ServiceInstanceRegistrationContext.builder()
                        .location(repositoryConfigurationProperties.host())
                        .port(repositoryConfigurationProperties.port())
                        .properties(properties)
                        .build()
        );
    }

    private void registerDocumentIndex() {
        // Running Elasticsearch is not mandatory for the application
        if (!indexDatabaseConfigurationProperties.host().isBlank()) {
            serviceInstanceContainer.registerServiceInstance(ApplicationType.DOCUMENT_INDEX,
                    ServiceInstanceRegistrationContext.builder()
                            .location(indexDatabaseConfigurationProperties.host())
                            .port(indexDatabaseConfigurationProperties.port())
                            .build()
            );
        }
    }
}
