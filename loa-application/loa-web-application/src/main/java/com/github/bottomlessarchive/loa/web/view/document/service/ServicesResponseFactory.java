package com.github.bottomlessarchive.loa.web.view.document.service;

import com.github.bottomlessarchive.loa.application.domain.ApplicationType;
import com.github.bottomlessarchive.loa.conductor.service.client.ConductorClient;
import com.github.bottomlessarchive.loa.conductor.service.domain.ServiceInstanceEntity;
import com.github.bottomlessarchive.loa.web.view.document.response.dashboard.AdministratorApplicationInstance;
import com.github.bottomlessarchive.loa.web.view.document.response.dashboard.DashboardApplicationsResponse;
import com.github.bottomlessarchive.loa.web.view.document.response.dashboard.DownloaderApplicationInstance;
import com.github.bottomlessarchive.loa.web.view.document.response.dashboard.GeneratorApplicationInstance;
import com.github.bottomlessarchive.loa.web.view.document.response.dashboard.IndexerApplicationInstance;
import com.github.bottomlessarchive.loa.web.view.document.response.dashboard.QueueApplicationInstance;
import com.github.bottomlessarchive.loa.web.view.document.response.dashboard.VaultApplicationInstance;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ServicesResponseFactory {

    private final ConductorClient conductorClient;

    public DashboardApplicationsResponse newApplicationsResponse() {
        final List<ServiceInstanceEntity> serviceInstanceEntities = conductorClient.getInstances();

        return DashboardApplicationsResponse.builder()
                .administrators(buildAdministrators(serviceInstanceEntities))
                .downloaders(buildDownloaders(serviceInstanceEntities))
                .generators(buildGenerators(serviceInstanceEntities))
                .indexers(buildIndexers(serviceInstanceEntities))
                .queues(buildQueues(serviceInstanceEntities))
                .vaults(buildVaults(serviceInstanceEntities))
                .build();
    }

    private List<AdministratorApplicationInstance> buildAdministrators(final List<ServiceInstanceEntity> serviceInstanceEntities) {
        return serviceInstanceEntities.stream()
                .filter(instance -> instance.getApplicationType().equals(ApplicationType.ADMINISTRATOR_APPLICATION))
                .map(instance -> AdministratorApplicationInstance.builder()
                        .host(instance.getLocation())
                        .port(instance.getPort())
                        .build()
                )
                .toList();
    }

    private List<DownloaderApplicationInstance> buildDownloaders(final List<ServiceInstanceEntity> serviceInstanceEntities) {
        return serviceInstanceEntities.stream()
                .filter(instance -> instance.getApplicationType().equals(ApplicationType.DOWNLOADER_APPLICATION))
                .map(instance -> DownloaderApplicationInstance.builder()
                        .host(instance.getLocation())
                        .port(instance.getPort())
                        .build()
                )
                .toList();
    }

    private List<GeneratorApplicationInstance> buildGenerators(final List<ServiceInstanceEntity> serviceInstanceEntities) {
        return serviceInstanceEntities.stream()
                .filter(instance -> instance.getApplicationType().equals(ApplicationType.GENERATOR_APPLICATION))
                .map(instance -> GeneratorApplicationInstance.builder()
                        .host(instance.getLocation())
                        .port(instance.getPort())
                        .build()
                )
                .toList();
    }

    private List<IndexerApplicationInstance> buildIndexers(final List<ServiceInstanceEntity> serviceInstanceEntities) {
        return serviceInstanceEntities.stream()
                .filter(instance -> instance.getApplicationType().equals(ApplicationType.INDEXER_APPLICATION))
                .map(instance -> IndexerApplicationInstance.builder()
                        .host(instance.getLocation())
                        .port(instance.getPort())
                        .build()
                )
                .toList();
    }

    private List<QueueApplicationInstance> buildQueues(final List<ServiceInstanceEntity> serviceInstanceEntities) {
        return serviceInstanceEntities.stream()
                .filter(instance -> instance.getApplicationType().equals(ApplicationType.QUEUE_APPLICATION))
                .map(instance -> QueueApplicationInstance.builder()
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

    private List<VaultApplicationInstance> buildVaults(final List<ServiceInstanceEntity> serviceInstanceEntities) {
        return serviceInstanceEntities.stream()
                .filter(instance -> instance.getApplicationType().equals(ApplicationType.VAULT_APPLICATION))
                .map(instance -> VaultApplicationInstance.builder()
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
