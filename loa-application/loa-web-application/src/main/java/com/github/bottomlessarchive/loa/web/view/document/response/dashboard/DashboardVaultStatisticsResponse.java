package com.github.bottomlessarchive.loa.web.view.document.response.dashboard;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class DashboardVaultStatisticsResponse {

    private final String name;
    private final long availableStorageInBytes;
}
