package com.github.bottomlessarchive.loa.web.view.document.service;

import com.github.bottomlessarchive.loa.application.domain.ApplicationType;
import com.github.bottomlessarchive.loa.conductor.service.client.ConductorClient;
import com.github.bottomlessarchive.loa.conductor.service.domain.ServiceInstanceEntity;
import com.github.bottomlessarchive.loa.web.view.document.response.dashboard.AdministratorServiceInstance;
import com.github.bottomlessarchive.loa.web.view.document.response.dashboard.DashboardServicesResponse;
import com.github.bottomlessarchive.loa.web.view.document.response.dashboard.DownloaderServiceInstance;
import com.github.bottomlessarchive.loa.web.view.document.response.dashboard.GeneratorServiceInstance;
import com.github.bottomlessarchive.loa.web.view.document.response.dashboard.IndexerServiceInstance;
import com.github.bottomlessarchive.loa.web.view.document.response.dashboard.QueueServiceInstance;
import com.github.bottomlessarchive.loa.web.view.document.response.dashboard.VaultServiceInstance;
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
                .administrators(buildAdministrators(serviceInstanceEntities))
                .downloaders(buildDownloaders(serviceInstanceEntities))
                .generators(buildGenerators(serviceInstanceEntities))
                .indexers(buildIndexers(serviceInstanceEntities))
                .queues(buildQueues(serviceInstanceEntities))
                .vaults(buildVaults(serviceInstanceEntities))
                .build();
    }

    private List<AdministratorServiceInstance> buildAdministrators(final List<ServiceInstanceEntity> serviceInstanceEntities) {
        return serviceInstanceEntities.stream()
                .filter(instance -> instance.getApplicationType().equals(ApplicationType.ADMINISTRATOR_APPLICATION))
                .map(instance -> AdministratorServiceInstance.builder()
                        .host(instance.getLocation())
                        .port(instance.getPort())
                        .build()
                )
                .toList();
    }

    private List<DownloaderServiceInstance> buildDownloaders(final List<ServiceInstanceEntity> serviceInstanceEntities) {
        return serviceInstanceEntities.stream()
                .filter(instance -> instance.getApplicationType().equals(ApplicationType.DOWNLOADER_APPLICATION))
                .map(instance -> DownloaderServiceInstance.builder()
                        .host(instance.getLocation())
                        .port(instance.getPort())
                        .build()
                )
                .toList();
    }

    private List<GeneratorServiceInstance> buildGenerators(final List<ServiceInstanceEntity> serviceInstanceEntities) {
        return serviceInstanceEntities.stream()
                .filter(instance -> instance.getApplicationType().equals(ApplicationType.GENERATOR_APPLICATION))
                .map(instance -> GeneratorServiceInstance.builder()
                        .host(instance.getLocation())
                        .port(instance.getPort())
                        .build()
                )
                .toList();
    }

    private List<IndexerServiceInstance> buildIndexers(final List<ServiceInstanceEntity> serviceInstanceEntities) {
        return serviceInstanceEntities.stream()
                .filter(instance -> instance.getApplicationType().equals(ApplicationType.INDEXER_APPLICATION))
                .map(instance -> IndexerServiceInstance.builder()
                        .host(instance.getLocation())
                        .port(instance.getPort())
                        .build()
                )
                .toList();
    }

    private List<QueueServiceInstance> buildQueues(final List<ServiceInstanceEntity> serviceInstanceEntities) {
        return serviceInstanceEntities.stream()
                .filter(instance -> instance.getApplicationType().equals(ApplicationType.QUEUE_APPLICATION))
                .map(instance -> QueueServiceInstance.builder()
                        .host(instance.getLocation())
                        .port(instance.getPort())
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

    private List<VaultServiceInstance> buildVaults(final List<ServiceInstanceEntity> serviceInstanceEntities) {
        return serviceInstanceEntities.stream()
                .filter(instance -> instance.getApplicationType().equals(ApplicationType.VAULT_APPLICATION))
                .map(instance -> VaultServiceInstance.builder()
                        .host(instance.getLocation())
                        .port(instance.getPort())
                        .freeSpace(
                                instance.getProperty("freeSpace")
                                        .map(property -> Long.valueOf(property.getValue()))
                                        .orElse(-1L)
                        )
                        .build()
                )
                .toList();
    }
}
