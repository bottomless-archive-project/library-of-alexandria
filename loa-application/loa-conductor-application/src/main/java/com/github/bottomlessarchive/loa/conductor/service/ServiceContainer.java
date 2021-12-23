package com.github.bottomlessarchive.loa.conductor.service;

import com.github.bottomlessarchive.loa.application.domain.ApplicationType;
import com.github.bottomlessarchive.loa.conductor.service.domain.ServiceInstanceEntity;
import com.github.bottomlessarchive.loa.conductor.service.domain.ServiceInstanceRegistrationContext;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Collections;
import java.util.EnumMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

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

    public List<ServiceInstanceEntity> queryServiceInstances(final ApplicationType applicationType) {
        return Collections.unmodifiableList(serviceMap.getOrDefault(applicationType, Collections.emptyList()));
    }
}
