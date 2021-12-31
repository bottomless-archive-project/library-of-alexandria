package com.github.bottomlessarchive.loa.conductor.view.controller;

import com.github.bottomlessarchive.loa.application.domain.ApplicationType;
import com.github.bottomlessarchive.loa.conductor.service.ServiceContainer;
import com.github.bottomlessarchive.loa.conductor.service.domain.ServiceInstanceEntity;
import com.github.bottomlessarchive.loa.conductor.service.domain.ServiceInstanceRegistrationContext;
import com.github.bottomlessarchive.loa.conductor.service.domain.ServiceInstanceRegistrationProperty;
import com.github.bottomlessarchive.loa.conductor.view.request.ServiceInstanceRegistrationRequest;
import com.github.bottomlessarchive.loa.conductor.view.response.ServiceInstancePropertyResponse;
import com.github.bottomlessarchive.loa.conductor.view.response.ServiceInstanceRegistrationResponse;
import com.github.bottomlessarchive.loa.conductor.view.response.ServiceInstanceResponse;
import com.github.bottomlessarchive.loa.conductor.view.response.ServiceResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@RestController
@RequestMapping("service")
@RequiredArgsConstructor
public class ServiceRegistryController {

    private final ServiceContainer serviceContainer;

    @PostMapping("/{applicationType}")
    public ServiceInstanceRegistrationResponse registerServiceInstance(@PathVariable final ApplicationType applicationType,
            @RequestBody final ServiceInstanceRegistrationRequest serviceInstanceRegistrationRequest) {
        if (serviceInstanceRegistrationRequest.getPort() > 65535) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Illegal port: "
                    + serviceInstanceRegistrationRequest.getPort() + "!");
        }

        if (serviceInstanceRegistrationRequest.getLocation().isEmpty()
                || serviceInstanceRegistrationRequest.getLocation().length() > 512) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Illegal location: "
                    + serviceInstanceRegistrationRequest.getLocation() + "!");
        }

        final UUID serviceInstanceId = serviceContainer.registerServiceInstance(applicationType,
                ServiceInstanceRegistrationContext.builder()
                        .location(serviceInstanceRegistrationRequest.getLocation())
                        .port(serviceInstanceRegistrationRequest.getPort())
                        .properties(serviceInstanceRegistrationRequest.getProperties().stream()
                                .map(serviceInstanceRegistrationPropertyRequest -> ServiceInstanceRegistrationProperty.builder()
                                        .name(serviceInstanceRegistrationPropertyRequest.getName())
                                        .value(serviceInstanceRegistrationPropertyRequest.getValue())
                                        .build()
                                )
                                .collect(Collectors.toList())
                        )
                        .build()
        );

        return ServiceInstanceRegistrationResponse.builder()
                .id(serviceInstanceId)
                .build();
    }

    @GetMapping
    public List<ServiceResponse> queryServices() {
        return Stream.of(ApplicationType.values())
                .map(applicationType -> {
                    final List<ServiceInstanceResponse> serviceInstanceResponses = serviceContainer.queryServiceInstances(applicationType).stream()
                            .map(serviceEntity -> ServiceInstanceResponse.builder()
                                    .id(serviceEntity.getId())
                                    .location(serviceEntity.getLocation())
                                    .port(serviceEntity.getPort())
                                    .properties(serviceEntity.getProperties().stream()
                                            .map(serviceInstanceEntityProperty -> ServiceInstancePropertyResponse.builder()
                                                    .name(serviceInstanceEntityProperty.getName())
                                                    .value(serviceInstanceEntityProperty.getValue())
                                                    .build()
                                            )
                                            .collect(Collectors.toList())
                                    )
                                    .lastHeartbeat(serviceEntity.getLastHeartbeat())
                                    .build()
                            )
                            .toList();

                    return ServiceResponse.builder()
                            .applicationType(applicationType)
                            .instances(serviceInstanceResponses)
                            .build();
                })
                .toList();
    }

    @GetMapping("/{applicationType}")
    public ServiceResponse queryServiceInstances(@PathVariable final ApplicationType applicationType) {
        final List<ServiceInstanceResponse> serviceInstanceResponses = serviceContainer.queryServiceInstances(applicationType).stream()
                .map(serviceEntity -> ServiceInstanceResponse.builder()
                        .id(serviceEntity.getId())
                        .location(serviceEntity.getLocation())
                        .port(serviceEntity.getPort())
                        .properties(serviceEntity.getProperties().stream()
                                .map(serviceInstanceEntityProperty -> ServiceInstancePropertyResponse.builder()
                                        .name(serviceInstanceEntityProperty.getName())
                                        .value(serviceInstanceEntityProperty.getValue())
                                        .build()
                                )
                                .collect(Collectors.toList())
                        )
                        .lastHeartbeat(serviceEntity.getLastHeartbeat())
                        .build()
                )
                .toList();

        return ServiceResponse.builder()
                .applicationType(applicationType)
                .instances(serviceInstanceResponses)
                .build();
    }

    @PutMapping("/{applicationType}/{instanceId}")
    public void refreshServiceInstance(@PathVariable final ApplicationType applicationType, @PathVariable final UUID instanceId) {
        serviceContainer.getServiceInstance(applicationType, instanceId)
                .ifPresent(ServiceInstanceEntity::refreshHeartbeat);
    }
}
