package com.github.bottomlessarchive.loa.conductor.view.controller;

import com.github.bottomlessarchive.loa.conductor.service.ServiceContainer;
import com.github.bottomlessarchive.loa.conductor.service.domain.ServiceInstanceEntity;
import com.github.bottomlessarchive.loa.conductor.service.domain.ServiceInstanceRegistrationContext;
import com.github.bottomlessarchive.loa.conductor.view.request.ServiceInstanceRegistrationRequest;
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
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
public class ServiceRegistryController {

    private final ServiceContainer serviceContainer;

    @PostMapping("/{serviceName}")
    public ServiceInstanceRegistrationResponse registerServiceInstance(@PathVariable final String serviceName,
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

        final UUID serviceInstanceId = serviceContainer.registerServiceInstance(serviceName,
                ServiceInstanceRegistrationContext.builder()
                        .location(serviceInstanceRegistrationRequest.getLocation())
                        .port(serviceInstanceRegistrationRequest.getPort())
                        .build()
        );

        return ServiceInstanceRegistrationResponse.builder()
                .id(serviceInstanceId)
                .build();
    }

    @GetMapping("/{serviceName}")
    public ServiceResponse queryServiceInstances(@PathVariable final String serviceName) {
        final List<ServiceInstanceResponse> serviceInstanceResponses = serviceContainer.queryServiceInstances(serviceName).stream()
                .map(serviceEntity -> ServiceInstanceResponse.builder()
                        .location(serviceEntity.getLocation())
                        .port(serviceEntity.getPort())
                        .lastHeartbeat(serviceEntity.getLastHeartbeat())
                        .build()
                )
                .toList();

        return ServiceResponse.builder()
                .name(serviceName)
                .instances(serviceInstanceResponses)
                .build();
    }

    @PutMapping("/{serviceName}/{instanceId}")
    public void refreshServiceInstance(@PathVariable final String serviceName, @PathVariable final UUID instanceId) {
        serviceContainer.queryServiceInstances(serviceName).stream()
                .filter(serviceInstanceEntity -> serviceInstanceEntity.getId().equals(instanceId))
                .findFirst()
                .ifPresent(ServiceInstanceEntity::refreshHeartbeat);
    }
}
