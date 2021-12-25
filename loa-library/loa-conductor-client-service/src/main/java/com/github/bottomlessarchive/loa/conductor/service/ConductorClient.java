package com.github.bottomlessarchive.loa.conductor.service;

import com.github.bottomlessarchive.loa.application.domain.ApplicationType;
import com.github.bottomlessarchive.loa.conductor.service.configuration.ConductorClientConfigurationProperties;
import com.github.bottomlessarchive.loa.conductor.service.domain.ServiceInstanceEntity;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Service
@RequiredArgsConstructor
public class ConductorClient {

    private final ConductorClientConfigurationProperties conductorClientConfigurationProperties;

    public Flux<ServiceInstanceEntity> getInstances(final ApplicationType applicationType) {
        return Flux.empty();
    }

    public Mono<Void> registerInstance(final ApplicationType applicationType) {
        return Mono.empty();
    }

    public Mono<Void> refreshInstance(final ApplicationType applicationType) {
        return Mono.empty();
    }
}
