package com.github.bottomlessarchive.loa.web.view.document.response.dashboard;

import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public class DashboardServicesResponse {

    private final List<AdministratorServiceInstance> administrators;
    private final List<DownloaderServiceInstance> downloaders;
    private final List<GeneratorServiceInstance> generators;
    private final List<IndexerServiceInstance> indexers;
    private final List<QueueServiceInstance> queues;
    private final List<VaultServiceInstance> vaults;
}
