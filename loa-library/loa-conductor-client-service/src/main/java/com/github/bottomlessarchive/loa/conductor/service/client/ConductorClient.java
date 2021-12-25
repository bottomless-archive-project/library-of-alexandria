package com.github.bottomlessarchive.loa.conductor.service.client;

import com.github.bottomlessarchive.loa.application.domain.ApplicationType;
import com.github.bottomlessarchive.loa.conductor.service.NetworkAddressCalculator;
import com.github.bottomlessarchive.loa.conductor.service.client.configuration.ConductorClientConfigurationProperties;
import com.github.bottomlessarchive.loa.conductor.service.client.request.ServiceInstanceRegistrationRequest;
import com.github.bottomlessarchive.loa.conductor.service.client.response.ServiceInstanceRegistrationResponse;
import com.github.bottomlessarchive.loa.conductor.service.client.response.ServiceResponse;
import com.github.bottomlessarchive.loa.conductor.service.domain.ServiceInstanceEntity;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.web.ServerProperties;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Locale;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ConductorClient {

    private final WebClient webClient;
    private final ServerProperties serverProperties;
    private final NetworkAddressCalculator networkAddressCalculator;
    private final ConductorClientConfigurationProperties conductorClientConfigurationProperties;

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
                                .lastHeartbeat(instance.getLastHeartbeat())
                                .build()
                        )
                );
    }

    public Mono<UUID> registerInstance(final ApplicationType applicationType) {
        final ServiceInstanceRegistrationRequest serviceInstanceRegistrationRequest = ServiceInstanceRegistrationRequest.builder()
                .location(networkAddressCalculator.calculateInetAddress().getHostAddress())
                .port(serverProperties.getPort())
                .build();

        return webClient.post()
                .uri(conductorClientConfigurationProperties.getUrl() + "/service/" + convertApplicationTypeToPath(applicationType))
                .body(BodyInserters.fromValue(serviceInstanceRegistrationRequest))
                .retrieve()
                .bodyToMono(ServiceInstanceRegistrationResponse.class)
                .map(ServiceInstanceRegistrationResponse::getId);
    }

    public Mono<Void> refreshInstance(final UUID instanceId, final ApplicationType applicationType) {
        return webClient.put()
                .uri(conductorClientConfigurationProperties.getUrl() + "/service/" + convertApplicationTypeToPath(applicationType)
                        + instanceId)
                .retrieve()
                .bodyToMono(Void.class)
                .then();
    }

    private String convertApplicationTypeToPath(final ApplicationType applicationType) {
        return applicationType.name().replaceAll("_", "-").toLowerCase(Locale.ENGLISH);
    }
}
