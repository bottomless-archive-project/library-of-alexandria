package com.github.loa.web.view.document.response.dashboard;

import com.github.loa.document.service.domain.DocumentStatus;
import com.github.loa.document.service.domain.DocumentType;
import lombok.Builder;
import lombok.Getter;

import java.util.Map;
import java.util.Set;

@Getter
@Builder
public class DashboardDocumentStatisticsResponse {

    private final long documentCount;
    private final Set<DashboardVaultStatisticsResponse> vaultInstances;
    private final Map<DocumentType, Integer> documentCountByType;
    private final Map<DocumentStatus, Integer> documentCountByStatus;
}
