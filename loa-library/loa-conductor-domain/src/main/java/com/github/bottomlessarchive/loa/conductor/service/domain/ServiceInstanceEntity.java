package com.github.bottomlessarchive.loa.conductor.service.domain;

import com.github.bottomlessarchive.loa.application.domain.ApplicationType;
import lombok.Builder;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import java.time.Instant;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Slf4j
@Getter
@Builder
public class ServiceInstanceEntity {

    private final UUID id;
    private final ApplicationType applicationType;
    private final String location;
    private final int port;
    private final List<ServiceInstanceEntityProperty> properties;

    private Instant lastHeartbeat;

    public void refreshHeartbeat() {
        log.info("Refreshing application instance type: {} with instanceId: {}.", applicationType, id);

        lastHeartbeat = Instant.now();
    }

    public List<ServiceInstanceEntityProperty> getProperties() {
        return Collections.unmodifiableList(properties);
    }

    public Optional<ServiceInstanceEntityProperty> getProperty(final String name) {
        return properties.stream()
                .filter(property -> property.getName().equals(name))
                .findFirst();
    }
}
