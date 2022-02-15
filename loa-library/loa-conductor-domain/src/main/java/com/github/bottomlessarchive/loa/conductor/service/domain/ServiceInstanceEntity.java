package com.github.bottomlessarchive.loa.conductor.service.domain;

import com.github.bottomlessarchive.loa.application.domain.ApplicationType;
import lombok.Builder;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Map;
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
    private final Map<String, String> properties;

    private Instant lastHeartbeat;

    public void refreshHeartbeat() {
        log.info("Refreshing application instance type: {} with instanceId: {}.", applicationType, id);

        lastHeartbeat = Instant.now();
    }

    public List<ServiceInstanceEntityProperty> getProperties() {
        return properties.entrySet().stream()
                .map(entry -> ServiceInstanceEntityProperty.builder()
                        .name(entry.getKey())
                        .value(entry.getValue())
                        .build()
                )
                .toList();
    }

    public void setProperties(final List<ServiceInstanceEntityProperty> serviceInstanceEntityProperties) {
        serviceInstanceEntityProperties.forEach(serviceInstanceEntityProperty ->
                properties.put(serviceInstanceEntityProperty.getName(), serviceInstanceEntityProperty.getValue()));
    }

    public Optional<ServiceInstanceEntityProperty> getProperty(final String name) {
        if (!properties.containsKey(name)) {
            return Optional.empty();
        }

        return Optional.of(
                ServiceInstanceEntityProperty.builder()
                        .name(name)
                        .value(properties.get(name))
                        .build()
        );
    }

    public boolean isHealthy() {
        return lastHeartbeat.plus(2, ChronoUnit.MINUTES).isAfter(Instant.now());
    }
}
