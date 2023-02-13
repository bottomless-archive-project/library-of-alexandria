package com.github.bottomlessarchive.loa.conductor.service;

import com.github.bottomlessarchive.loa.conductor.service.client.ConductorClient;
import com.github.bottomlessarchive.loa.conductor.service.client.configuration.ConductorClientConfigurationProperties;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Profile;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
public class InstanceRefreshSchedulerService {

    private final ConductorClient conductorClient;
    private final ConductorClientConfigurationProperties conductorClientConfigurationProperties;

    private UUID instanceId;

    @Scheduled(fixedRate = 60, timeUnit = TimeUnit.SECONDS, initialDelay = 10)
    public void refreshInstance() {
        if (instanceId == null) {
            instanceId = conductorClient.registerInstance(conductorClientConfigurationProperties.applicationType());
        } else {
            conductorClient.refreshInstance(instanceId, conductorClientConfigurationProperties.applicationType());
        }
    }
}
