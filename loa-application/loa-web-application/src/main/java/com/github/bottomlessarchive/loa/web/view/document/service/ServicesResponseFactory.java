package com.github.bottomlessarchive.loa.web.view.document.service;

import com.github.bottomlessarchive.loa.application.domain.ApplicationType;
import com.github.bottomlessarchive.loa.conductor.service.client.ConductorClient;
import com.github.bottomlessarchive.loa.conductor.service.domain.ServiceInstanceEntity;
import com.github.bottomlessarchive.loa.web.view.document.response.dashboard.DashboardServicesResponse;
import com.github.bottomlessarchive.loa.web.view.document.response.dashboard.QueueServiceInstance;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ServicesResponseFactory {

    private final ConductorClient conductorClient;

    public DashboardServicesResponse newServicesResponse() {
        final List<ServiceInstanceEntity> serviceInstanceEntities = conductorClient.getInstances();

        return DashboardServicesResponse.builder()
                .queues(buildQueues(serviceInstanceEntities))
                .build();
    }

    private List<QueueServiceInstance> buildQueues(final List<ServiceInstanceEntity> serviceInstanceEntities) {
        return serviceInstanceEntities.stream()
                .filter(instance -> instance.getApplicationType().equals(ApplicationType.QUEUE_APPLICATION))
                .map(instance -> QueueServiceInstance.builder()
                        .documentLocationQueueCount(
                                instance.getProperty("locationQueueCount")
                                        .map(property -> Long.valueOf(property.getValue()))
                                        .orElse(-1L)
                        )
                        .documentArchivingQueueCount(
                                instance.getProperty("archivingQueueCount")
                                        .map(property -> Long.valueOf(property.getValue()))
                                        .orElse(-1L)
                        )
                        .build()
                )
                .toList();
    }
}
