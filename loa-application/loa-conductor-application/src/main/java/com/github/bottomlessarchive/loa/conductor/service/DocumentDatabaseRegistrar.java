package com.github.bottomlessarchive.loa.conductor.service;

import com.github.bottomlessarchive.loa.application.domain.ApplicationType;
import com.github.bottomlessarchive.loa.conductor.configuration.RepositoryConfigurationProperties;
import com.github.bottomlessarchive.loa.conductor.service.domain.ServiceInstanceProperty;
import com.github.bottomlessarchive.loa.conductor.service.domain.ServiceInstanceRegistrationContext;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class DocumentDatabaseRegistrar implements ApplicationListener<ContextRefreshedEvent> {

    private final ServiceInstanceContainer serviceInstanceContainer;
    private final RepositoryConfigurationProperties repositoryConfigurationProperties;

    @Override
    public void onApplicationEvent(@NonNull final ContextRefreshedEvent event) {
        serviceInstanceContainer.registerServiceInstance(ApplicationType.DOCUMENT_DATABASE,
                ServiceInstanceRegistrationContext.builder()
                        .location(repositoryConfigurationProperties.host())
                        .port(repositoryConfigurationProperties.port())
                        .properties(
                                List.of(
                                        ServiceInstanceProperty.builder()
                                                .name("uri")
                                                .value(repositoryConfigurationProperties.uri())
                                                .build(),
                                        ServiceInstanceProperty.builder()
                                                .name("noCursorTimeout")
                                                .value(String.valueOf(repositoryConfigurationProperties.noCursorTimeout()))
                                                .build()
                                )
                        )
                        .build()
        );
    }
}
