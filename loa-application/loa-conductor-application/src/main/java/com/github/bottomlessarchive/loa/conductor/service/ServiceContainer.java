package com.github.bottomlessarchive.loa.conductor.service;

import com.github.bottomlessarchive.loa.conductor.service.domain.ServiceInstanceEntity;
import com.github.bottomlessarchive.loa.conductor.service.domain.ServiceInstanceRegistrationContext;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
public class ServiceContainer {

    private final Map<String, List<ServiceInstanceEntity>> serviceMap = new HashMap<>();

    public UUID registerServiceInstance(final String serviceName,
            final ServiceInstanceRegistrationContext serviceInstanceRegistrationContext) {
        final UUID serviceInstanceId = UUID.randomUUID();

        if (!serviceMap.containsKey(serviceName)) {
            serviceMap.put(serviceName, new LinkedList<>());
        }

        serviceMap.get(serviceName)
                .add(
                        ServiceInstanceEntity.builder()
                                .id(serviceInstanceId)
                                .name(serviceName)
                                .location(serviceInstanceRegistrationContext.getLocation())
                                .port(serviceInstanceRegistrationContext.getPort())
                                .lastHeartbeat(Instant.now())
                                .build()
                );

        return serviceInstanceId;
    }

    public List<ServiceInstanceEntity> queryServiceInstances(final String serviceName) {
        return Collections.unmodifiableList(serviceMap.getOrDefault(serviceName, Collections.emptyList()));
    }
}
