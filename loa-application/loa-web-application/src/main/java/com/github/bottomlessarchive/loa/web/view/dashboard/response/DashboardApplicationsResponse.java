package com.github.bottomlessarchive.loa.web.view.dashboard.response;

import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public class DashboardApplicationsResponse {

    private final List<AdministratorApplicationInstance> administrators;
    private final List<DownloaderApplicationInstance> downloaders;
    private final List<GeneratorApplicationInstance> generators;
    private final List<IndexerApplicationInstance> indexers;
    private final List<QueueApplicationInstance> queues;
    private final List<VaultApplicationInstance> vaults;
}
