package com.github.bottomlessarchive.loa.conductor.service;

import com.github.bottomlessarchive.loa.application.domain.ApplicationType;
import com.github.bottomlessarchive.loa.conductor.service.domain.ServiceInstanceEntity;
import com.github.bottomlessarchive.loa.conductor.service.domain.ServiceInstanceRegistrationContext;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Collections;
import java.util.EnumMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@Slf4j
@Service
public class ServiceContainer {

    private final Map<ApplicationType, List<ServiceInstanceEntity>> serviceMap = new EnumMap<>(ApplicationType.class);

    public UUID registerServiceInstance(final ApplicationType applicationType,
            final ServiceInstanceRegistrationContext serviceInstanceRegistrationContext) {
        final UUID serviceInstanceId = UUID.randomUUID();

        if (!serviceMap.containsKey(applicationType)) {
            serviceMap.put(applicationType, new LinkedList<>());
        }

        serviceMap.get(applicationType)
                .add(
                        ServiceInstanceEntity.builder()
                                .id(serviceInstanceId)
                                .applicationType(applicationType)
                                .location(serviceInstanceRegistrationContext.getLocation())
                                .port(serviceInstanceRegistrationContext.getPort())
                                .lastHeartbeat(Instant.now())
                                .build()
                );

        return serviceInstanceId;
    }

    public Optional<ServiceInstanceEntity> getServiceInstance(final ApplicationType applicationType, final UUID instanceId) {
        return serviceMap.getOrDefault(applicationType, Collections.emptyList()).stream()
                .filter(serviceInstanceEntity -> serviceInstanceEntity.getId().equals(instanceId))
                .findFirst();
    }

    public List<ServiceInstanceEntity> queryServiceInstances(final ApplicationType applicationType) {
        return Collections.unmodifiableList(serviceMap.getOrDefault(applicationType, Collections.emptyList()));
    }

    public void cleanupServiceInstances() {
        log.info("Running service instance cleanup.");

        final Instant removeUntil = Instant.now().minus(2, ChronoUnit.MINUTES);

        serviceMap.values()
                .forEach(list -> list.removeIf(instance -> {
                    if (instance.getLastHeartbeat().isBefore(removeUntil)) {
                        log.info("Removing timed-out instance for application type: {} and instanceId: {}.", instance.getApplicationType(),
                                instance.getId());

                        return true;
                    }

                    return false;
                }));
    }
}
