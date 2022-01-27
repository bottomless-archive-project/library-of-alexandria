package com.github.bottomlessarchive.loa.conductor.view.service;

import com.github.bottomlessarchive.loa.conductor.service.domain.ServiceInstanceEntity;
import com.github.bottomlessarchive.loa.conductor.view.response.ServiceInstancePropertyResponse;
import com.github.bottomlessarchive.loa.conductor.view.response.ServiceInstanceResponse;
import org.springframework.stereotype.Service;

import java.util.stream.Collectors;

@Service
public class ServiceInstanceEntityTransformer {

    public ServiceInstanceResponse transform(final ServiceInstanceEntity serviceInstanceEntity) {
        return ServiceInstanceResponse.builder()
                .id(serviceInstanceEntity.getId())
                .location(serviceInstanceEntity.getLocation())
                .port(serviceInstanceEntity.getPort())
                .properties(serviceInstanceEntity.getProperties().stream()
                        .map(serviceInstanceEntityProperty -> ServiceInstancePropertyResponse.builder()
                                .name(serviceInstanceEntityProperty.getName())
                                .value(serviceInstanceEntityProperty.getValue())
                                .build()
                        )
                        .collect(Collectors.toList())
                )
                .lastHeartbeat(serviceInstanceEntity.getLastHeartbeat())
                .build();
    }
}
