package com.github.bottomlessarchive.loa.repository.configuration;

import com.github.bottomlessarchive.loa.application.domain.ApplicationType;
import com.github.bottomlessarchive.loa.conductor.service.client.ConductorClient;
import com.github.bottomlessarchive.loa.conductor.service.domain.ServiceInstanceEntity;
import lombok.Getter;
import org.springframework.stereotype.Component;

@Component
public class RepositoryMetadataContainer {

    @Getter
    private final boolean noCursorTimeout;

    public RepositoryMetadataContainer(final ConductorClient conductorClient) {
        final ServiceInstanceEntity serviceInstanceEntity = conductorClient.getInstance(ApplicationType.DOCUMENT_DATABASE)
                .orElseThrow(() -> new IllegalStateException("Document database (MongoDB) server is not available!"));

        noCursorTimeout = serviceInstanceEntity.getProperty("noCursorTimeout")
                .map(serviceInstanceEntityProperty -> Boolean.valueOf(serviceInstanceEntityProperty.getValue()))
                .orElse(false);
    }
}
