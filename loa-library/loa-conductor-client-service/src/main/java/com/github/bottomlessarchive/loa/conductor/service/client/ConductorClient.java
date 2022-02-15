package com.github.bottomlessarchive.loa.conductor.service.client;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
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
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class ConductorClient {

    @Qualifier("conductorWebClient")
    private final OkHttpClient okHttpClient;

    private final ObjectMapper objectMapper;
    private final NetworkAddressCalculator networkAddressCalculator;
    private final ConductorClientConfigurationProperties conductorClientConfigurationProperties;
    private final List<InstancePropertyExtensionProvider> instancePropertyExtensionProviderList;

    public Optional<ServiceInstanceEntity> getInstance(final ApplicationType applicationType) {
        return getInstances(applicationType).stream()
                .findFirst();
    }

    @SneakyThrows //TODO: Use ConductorClientException instead
    public List<ServiceInstanceEntity> getInstances(final ApplicationType applicationType) {
        final Request request = new Request.Builder()
                .url(conductorClientConfigurationProperties.getUrl() + "/service/" + convertApplicationTypeToPath(applicationType))
                .get()
                .build();

        final String response = okHttpClient.newCall(request)
                .execute()
                .body()
                .string();

        return objectMapper.readValue(response, ServiceResponse.class).getInstances().stream()
                .map(instance -> ServiceInstanceEntity.builder()
                        .id(instance.getId())
                        .location(instance.getLocation())
                        .port(instance.getPort())
                        .applicationType(applicationType)
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
                .toList();
    }

    @SneakyThrows //TODO: Use ConductorClientException instead
    public UUID registerInstance(final ApplicationType applicationType) {
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


        final Request request = new Request.Builder()
                .url(conductorClientConfigurationProperties.getUrl() + "/service/" + convertApplicationTypeToPath(applicationType))
                .post(createJsonBody(serviceInstanceRegistrationRequest))
                .build();

        final String response = okHttpClient.newCall(request)
                .execute()
                .body()
                .string();

        final ServiceInstanceRegistrationResponse serviceInstanceRegistrationResponse = objectMapper.readValue(
                response, ServiceInstanceRegistrationResponse.class);

        log.info("Registration was successful! Instance id: {}.", serviceInstanceRegistrationResponse.getId());

        return serviceInstanceRegistrationResponse.getId();
    }

    @SneakyThrows //TODO: Use ConductorClientException instead
    public void refreshInstance(final UUID instanceId, final ApplicationType applicationType) {
        log.info("Refreshing application instance type: {} with instanceId: {} in the Conductor Application.",
                applicationType, instanceId);

        final String hostAddress = networkAddressCalculator.calculateInetAddress().getHostAddress();

        final InstanceExtensionContext instanceExtensionContext = new InstanceExtensionContext();

        instancePropertyExtensionProviderList.forEach(instancePropertyExtensionProvider ->
                instancePropertyExtensionProvider.extendInstanceWithProperty(instanceExtensionContext));

        final ServiceInstanceRefreshRequest serviceInstanceRefreshRequest = ServiceInstanceRefreshRequest.builder()
                .location(hostAddress)
                .port(conductorClientConfigurationProperties.applicationPort())
                .properties(instanceExtensionContext.getProperties().entrySet().stream()
                        .map(entry -> ServiceInstancePropertyRequest.builder()
                                .name(entry.getKey())
                                .value(entry.getValue())
                                .build()
                        )
                        .toList()
                )
                .build();


        final Request request = new Request.Builder()
                .url(conductorClientConfigurationProperties.getUrl() + "/service/" + convertApplicationTypeToPath(applicationType)
                        + "/" + instanceId)
                .put(createJsonBody(serviceInstanceRefreshRequest))
                .build();

        okHttpClient.newCall(request)
                .execute()
                .close();
    }

    private RequestBody createJsonBody(final Object requestBody) throws JsonProcessingException {
        return RequestBody.create(objectMapper.writeValueAsBytes(requestBody), MediaType.get("application/json"));
    }

    private String convertApplicationTypeToPath(final ApplicationType applicationType) {
        return applicationType.name().replaceAll("_", "-").toLowerCase(Locale.ENGLISH);
    }
}
