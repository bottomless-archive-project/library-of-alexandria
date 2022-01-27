package com.github.bottomlessarchive.loa.conductor.view.controller;

import com.github.bottomlessarchive.loa.application.domain.ApplicationType;
import com.github.bottomlessarchive.loa.conductor.service.ServiceInstanceContainer;
import com.github.bottomlessarchive.loa.conductor.service.domain.ServiceInstanceRefreshContext;
import com.github.bottomlessarchive.loa.conductor.service.domain.ServiceInstanceProperty;
import com.github.bottomlessarchive.loa.conductor.service.domain.ServiceInstanceRegistrationContext;
import com.github.bottomlessarchive.loa.conductor.view.request.ServiceInstanceRefreshRequest;
import com.github.bottomlessarchive.loa.conductor.view.request.ServiceInstanceRegistrationRequest;
import com.github.bottomlessarchive.loa.conductor.view.response.ServiceInstanceRegistrationResponse;
import com.github.bottomlessarchive.loa.conductor.view.response.ServiceInstanceResponse;
import com.github.bottomlessarchive.loa.conductor.view.response.ServiceResponse;
import com.github.bottomlessarchive.loa.conductor.view.service.ServiceInstanceEntityTransformer;
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
import java.util.stream.Stream;

@RestController
@RequestMapping("service")
@RequiredArgsConstructor
public class ServiceRegistryController {

    private final ServiceInstanceContainer serviceInstanceContainer;
    private final ServiceInstanceEntityTransformer serviceInstanceEntityTransformer;

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

        final UUID serviceInstanceId = serviceInstanceContainer.registerServiceInstance(applicationType,
                ServiceInstanceRegistrationContext.builder()
                        .location(serviceInstanceRegistrationRequest.getLocation())
                        .port(serviceInstanceRegistrationRequest.getPort())
                        .properties(serviceInstanceRegistrationRequest.getProperties().stream()
                                .map(serviceInstanceRegistrationPropertyRequest -> ServiceInstanceProperty.builder()
                                        .name(serviceInstanceRegistrationPropertyRequest.getName())
                                        .value(serviceInstanceRegistrationPropertyRequest.getValue())
                                        .build()
                                )
                                .toList()
                        )
                        .build()
        );

        return ServiceInstanceRegistrationResponse.builder()
                .id(serviceInstanceId)
                .build();
    }

    @PutMapping("/{applicationType}/{instanceId}")
    public void refreshServiceInstance(@PathVariable final ApplicationType applicationType, @PathVariable final UUID instanceId,
            @RequestBody final ServiceInstanceRefreshRequest serviceInstanceRefreshRequest) {
        serviceInstanceContainer.refreshServiceInstance(
                ServiceInstanceRefreshContext.builder()
                        .instanceId(instanceId)
                        .applicationType(applicationType)
                        .properties(serviceInstanceRefreshRequest.getProperties().stream()
                                .map(serviceInstanceRegistrationPropertyRequest -> ServiceInstanceProperty.builder()
                                        .name(serviceInstanceRegistrationPropertyRequest.getName())
                                        .value(serviceInstanceRegistrationPropertyRequest.getValue())
                                        .build()
                                )
                                .toList()
                        )
                        .build()
        );
    }

    @GetMapping
    public List<ServiceResponse> queryServices() {
        return Stream.of(ApplicationType.values())
                .filter(ApplicationType::isReportStatusAndLocation)
                .map(applicationType -> {
                    final List<ServiceInstanceResponse> serviceInstanceResponses =
                            serviceInstanceContainer.queryServiceInstances(applicationType).stream()
                                    .map(serviceInstanceEntityTransformer::transform)
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
        final List<ServiceInstanceResponse> serviceInstanceResponses =
                serviceInstanceContainer.queryServiceInstances(applicationType).stream()
                        .map(serviceInstanceEntityTransformer::transform)
                        .toList();

        return ServiceResponse.builder()
                .applicationType(applicationType)
                .instances(serviceInstanceResponses)
                .build();
    }
}
