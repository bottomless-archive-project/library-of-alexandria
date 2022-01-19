package com.github.bottomlessarchive.loa.conductor.service.client;

import com.github.bottomlessarchive.loa.application.domain.ApplicationType;
import com.github.bottomlessarchive.loa.conductor.service.NetworkAddressCalculator;
import com.github.bottomlessarchive.loa.conductor.service.client.configuration.ConductorClientConfigurationProperties;
import com.github.bottomlessarchive.loa.conductor.service.client.extension.InstancePropertyExtensionProvider;
import com.github.bottomlessarchive.loa.conductor.service.client.extension.domain.InstanceExtensionContext;
import com.github.bottomlessarchive.loa.conductor.service.client.request.ServiceInstancePropertyRequest;
import com.github.bottomlessarchive.loa.conductor.service.client.request.ServiceInstanceRefreshRequest;
import com.github.bottomlessarchive.loa.conductor.service.client.request.ServiceInstanceRegistrationRequest;
import com.github.bottomlessarchive.loa.conductor.service.client.response.ServiceInstanceRegistrationResponse;
import com.github.bottomlessarchive.loa.conductor.service.client.response.ServiceResponse;
import com.github.bottomlessarchive.loa.conductor.service.domain.ServiceInstanceEntity;
import com.github.bottomlessarchive.loa.conductor.service.domain.ServiceInstanceEntityProperty;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Locale;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class ConductorClient {

    @Qualifier("conductorWebClient")
    private final WebClient webClient;

    private final NetworkAddressCalculator networkAddressCalculator;
    private final ConductorClientConfigurationProperties conductorClientConfigurationProperties;
    private final List<InstancePropertyExtensionProvider> instancePropertyExtensionProviderList;

    public Mono<ServiceInstanceEntity> getInstance(final ApplicationType applicationType) {
        return getInstances(applicationType)
                .next();
    }

    public Flux<ServiceInstanceEntity> getInstances(final ApplicationType applicationType) {
        return webClient.get()
                .uri(conductorClientConfigurationProperties.getUrl() + "/service/" + convertApplicationTypeToPath(applicationType))
                .retrieve()
                .bodyToFlux(ServiceResponse.class)
                .flatMap(response -> Flux.fromIterable(response.getInstances())
                        .map(instance -> ServiceInstanceEntity.builder()
                                .id(instance.getId())
                                .location(instance.getLocation())
                                .port(instance.getPort())
                                .applicationType(response.getApplicationType())
                                .properties(instance.getProperties().stream()
                                        .map(serviceInstancePropertyResponse -> ServiceInstanceEntityProperty.builder()
                                                .name(serviceInstancePropertyResponse.getName())
                                                .value(serviceInstancePropertyResponse.getValue())
                                                .build()
                                        )
                                        .collect(Collectors.toMap(
                                                ServiceInstanceEntityProperty::getName, ServiceInstanceEntityProperty::getValue))
                                )
                                .lastHeartbeat(instance.getLastHeartbeat())
                                .build()
                        )
                );
    }

    public Mono<UUID> registerInstance(final ApplicationType applicationType) {
        final String hostAddress = networkAddressCalculator.calculateInetAddress().getHostAddress();

        final InstanceExtensionContext instanceRegistrationContext = new InstanceExtensionContext();

        instancePropertyExtensionProviderList.forEach(instancePropertyExtensionProvider ->
                instancePropertyExtensionProvider.extendInstanceWithProperty(instanceRegistrationContext));

        final ServiceInstanceRegistrationRequest serviceInstanceRegistrationRequest = ServiceInstanceRegistrationRequest.builder()
                .location(hostAddress)
                .port(conductorClientConfigurationProperties.applicationPort())
                .properties(instanceRegistrationContext.getProperties().entrySet().stream()
                        .map(entry -> ServiceInstancePropertyRequest.builder()
                                .name(entry.getKey())
                                .value(entry.getValue())
                                .build()
                        )
                        .toList()
                )
                .build();

        log.info("Registering service {} with host address: {} and port: {} into the Conductor Application.", applicationType, hostAddress,
                conductorClientConfigurationProperties.applicationPort());

        return webClient.post()
                .uri(conductorClientConfigurationProperties.getUrl() + "/service/" + convertApplicationTypeToPath(applicationType))
                .body(BodyInserters.fromValue(serviceInstanceRegistrationRequest))
                .retrieve()
                .bodyToMono(ServiceInstanceRegistrationResponse.class)
                .map(ServiceInstanceRegistrationResponse::getId);
    }

    public Mono<Void> refreshInstance(final UUID instanceId, final ApplicationType applicationType) {
        log.info("Refreshing application instance type: {} with instanceId: {} in the Conductor Application.",
                applicationType, instanceId);

        final InstanceExtensionContext instanceExtensionContext = new InstanceExtensionContext();

        instancePropertyExtensionProviderList.forEach(instancePropertyExtensionProvider ->
                instancePropertyExtensionProvider.extendInstanceWithProperty(instanceExtensionContext));

        final ServiceInstanceRefreshRequest serviceInstanceRefreshRequest = ServiceInstanceRefreshRequest.builder()
                .properties(instanceExtensionContext.getProperties().entrySet().stream()
                        .map(entry -> ServiceInstancePropertyRequest.builder()
                                .name(entry.getKey())
                                .value(entry.getValue())
                                .build()
                        )
                        .toList()
                )
                .build();

        return webClient.put()
                .uri(conductorClientConfigurationProperties.getUrl() + "/service/" + convertApplicationTypeToPath(applicationType)
                        + "/" + instanceId)
                .body(BodyInserters.fromValue(serviceInstanceRefreshRequest))
                .retrieve()
                .bodyToMono(Void.class)
                .then();
    }

    private String convertApplicationTypeToPath(final ApplicationType applicationType) {
        return applicationType.name().replaceAll("_", "-").toLowerCase(Locale.ENGLISH);
    }
}
