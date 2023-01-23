package com.github.bottomlessarchive.loa.conductor.service.client;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.bottomlessarchive.loa.application.domain.ApplicationType;
import com.github.bottomlessarchive.loa.conductor.service.NetworkAddressCalculator;
import com.github.bottomlessarchive.loa.conductor.service.client.configuration.ConductorClientConfigurationProperties;
import com.github.bottomlessarchive.loa.conductor.service.client.domain.ConductorClientException;
import com.github.bottomlessarchive.loa.conductor.service.client.extension.InstancePropertyExtensionProvider;
import com.github.bottomlessarchive.loa.conductor.service.client.extension.domain.InstanceExtensionContext;
import com.github.bottomlessarchive.loa.conductor.service.client.request.ServiceInstancePropertyRequest;
import com.github.bottomlessarchive.loa.conductor.service.client.request.ServiceInstanceRefreshRequest;
import com.github.bottomlessarchive.loa.conductor.service.client.request.ServiceInstanceRegistrationRequest;
import com.github.bottomlessarchive.loa.conductor.service.client.response.ServiceInstanceRegistrationResponse;
import com.github.bottomlessarchive.loa.conductor.service.client.response.ServiceInstanceResponse;
import com.github.bottomlessarchive.loa.conductor.service.client.response.ServiceResponse;
import com.github.bottomlessarchive.loa.conductor.service.domain.ServiceInstanceEntity;
import com.github.bottomlessarchive.loa.conductor.service.domain.ServiceInstanceEntityProperty;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
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

    @Cacheable(cacheNames = "single-valid-application-instance", sync = true)
    public ServiceInstanceEntity getInstanceOrBlock(final ApplicationType applicationType) {
        while (true) {
            log.info("Requesting instance for application: {} in a blocking way.", applicationType);

            final Optional<ServiceInstanceEntity> instance = getInstance(applicationType);

            if (instance.isEmpty()) {
                try {
                    Thread.sleep(62000);
                } catch (InterruptedException e) {
                    throw new ConductorClientException("Error while querying the Conductor Application!", e);
                }
            } else {
                log.info("Got an instance: {} for the blocking call.", instance.get());

                return instance.get();
            }
        }
    }

    /**
     * Return a service instance for the provided application.
     *
     * @param applicationType the application to get the instance for
     * @return the service instance
     */
    @Cacheable(cacheNames = "single-application-instance", sync = true)
    public Optional<ServiceInstanceEntity> getInstance(final ApplicationType applicationType) {
        log.info("Requesting instance for application: {}.", applicationType);

        return getInstances(applicationType).stream()
                .findFirst();
    }

    @Cacheable(cacheNames = "single-application-instances", sync = true)
    public List<ServiceInstanceEntity> getInstances(final ApplicationType applicationType) {
        log.info("Requesting instances for application: {}.", applicationType);

        final Request request = new Request.Builder()
                .url(conductorClientConfigurationProperties.getUrl() + "/service/" + convertApplicationTypeToPath(applicationType))
                .get()
                .build();

        try {
            final String response = okHttpClient.newCall(request)
                    .execute()
                    .body()
                    .string();

            return objectMapper.readValue(response, ServiceResponse.class).getInstances().stream()
                    .map(instance -> transform(applicationType, instance))
                    .toList();
        } catch (IOException e) {
            throw new ConductorClientException("Error while querying the Conductor Application!", e);
        }
    }

    @Cacheable(cacheNames = "all-application-instances", sync = true)
    public List<ServiceInstanceEntity> getInstances() {
        log.info("Requesting instances for all applications.");

        final Request request = new Request.Builder()
                .url(conductorClientConfigurationProperties.getUrl() + "/service")
                .get()
                .build();

        try {
            final String response = okHttpClient.newCall(request)
                    .execute()
                    .body()
                    .string();

            return objectMapper.readValue(response, new TypeReference<List<ServiceResponse>>() {
                    }).stream()
                    .flatMap(serviceResponse -> serviceResponse.getInstances().stream()
                            .map(instance -> transform(serviceResponse.getApplicationType(), instance))
                    )
                    .toList();
        } catch (IOException e) {
            throw new ConductorClientException("Error while querying the Conductor Application!", e);
        }
    }

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

        try {
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
        } catch (IOException e) {
            throw new ConductorClientException("Error while querying the Conductor Application!", e);
        }
    }

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

        try {
            final Request request = new Request.Builder()
                    .url(conductorClientConfigurationProperties.getUrl() + "/service/" + convertApplicationTypeToPath(applicationType)
                            + "/" + instanceId)
                    .put(createJsonBody(serviceInstanceRefreshRequest))
                    .build();

            okHttpClient.newCall(request)
                    .execute()
                    .close();
        } catch (IOException e) {
            throw new ConductorClientException("Error while querying the Conductor Application!", e);
        }
    }

    @CacheEvict(value = {
            "single-valid-application-instance",
            "single-application-instance",
            "single-application-instances",
            "all-application-instances"
    })
    @Scheduled(fixedRate = 60, timeUnit = TimeUnit.SECONDS)
    public void clearCaches() {
        log.info("Clearing conductor related caches.");
    }

    private RequestBody createJsonBody(final Object requestBody) throws JsonProcessingException {
        return RequestBody.create(objectMapper.writeValueAsBytes(requestBody), MediaType.get("application/json"));
    }

    private String convertApplicationTypeToPath(final ApplicationType applicationType) {
        return applicationType.name().replaceAll("_", "-").toLowerCase(Locale.ENGLISH);
    }

    public ServiceInstanceEntity transform(final ApplicationType applicationType, final ServiceInstanceResponse instance) {
        return ServiceInstanceEntity.builder()
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
                .build();
    }
}
