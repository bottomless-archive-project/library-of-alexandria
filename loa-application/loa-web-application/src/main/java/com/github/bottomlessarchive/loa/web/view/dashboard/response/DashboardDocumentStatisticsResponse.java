package com.github.bottomlessarchive.loa.web.view.dashboard.response;

import com.github.bottomlessarchive.loa.document.service.domain.DocumentStatus;
import com.github.bottomlessarchive.loa.type.domain.DocumentType;
import lombok.Builder;
import lombok.Getter;

import java.util.Map;

@Getter
@Builder
public class DashboardDocumentStatisticsResponse {

    private final long documentCount;
    private final Map<DocumentType, Integer> documentCountByType;
    private final Map<DocumentStatus, Integer> documentCountByStatus;
}
