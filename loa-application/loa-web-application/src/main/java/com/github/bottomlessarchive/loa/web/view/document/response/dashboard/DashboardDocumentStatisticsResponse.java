package com.github.bottomlessarchive.loa.web.view.document.response.dashboard;

import com.github.bottomlessarchive.loa.document.service.domain.DocumentStatus;
import com.github.bottomlessarchive.loa.document.service.domain.DocumentType;
import lombok.Builder;
import lombok.Getter;

import java.util.List;
import java.util.Map;

@Getter
@Builder
public class DashboardDocumentStatisticsResponse {

    private final long documentCount;
    private final List<DashboardVaultStatisticsResponse> vaultInstances;
    private final List<DashboardQueueStatisticsResponse> queues;
    private final Map<DocumentType, Integer> documentCountByType;
    private final Map<DocumentStatus, Integer> documentCountByStatus;
}
