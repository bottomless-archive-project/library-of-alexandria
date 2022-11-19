package com.github.bottomlessarchive.loa.web.view.dashboard.service;

import com.github.bottomlessarchive.loa.document.service.domain.DocumentStatus;
import com.github.bottomlessarchive.loa.document.service.entity.factory.DocumentEntityFactory;
import com.github.bottomlessarchive.loa.type.domain.DocumentType;
import com.github.bottomlessarchive.loa.web.view.dashboard.response.DashboardDocumentStatisticsResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.Map;
import java.util.TreeMap;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class DocumentStatisticsResponseFactory {
    private final DocumentEntityFactory documentEntityFactory;

    public DashboardDocumentStatisticsResponse newStatisticsResponse() {
        final DashboardDocumentStatisticsResponse.DashboardDocumentStatisticsResponseBuilder builder =
                DashboardDocumentStatisticsResponse.builder();

        fillDocumentCount(builder);
        fillDocumentCountByStatus(builder);
        fillDocumentCountByType(builder);

        return builder.build();
    }

    private void fillDocumentCount(
            final DashboardDocumentStatisticsResponse.DashboardDocumentStatisticsResponseBuilder builder) {
        builder.documentCount(documentEntityFactory.getEstimatedDocumentCount());
    }

    private void fillDocumentCountByType(
            final DashboardDocumentStatisticsResponse.DashboardDocumentStatisticsResponseBuilder builder) {
        final Map<DocumentType, Integer> typeMap = documentEntityFactory.getCountByType();

        builder.documentCountByType(Arrays.stream(DocumentType.values())
                .collect(
                        Collectors.toMap(
                                documentType -> documentType,
                                documentType -> typeMap.getOrDefault(documentType, 0), (a, b) -> b, TreeMap::new
                        )
                )
        );
    }

    private void fillDocumentCountByStatus(
            final DashboardDocumentStatisticsResponse.DashboardDocumentStatisticsResponseBuilder builder) {
        final Map<DocumentStatus, Integer> statusMap = documentEntityFactory.getCountByStatus();

        builder.documentCountByStatus(Arrays.stream(DocumentStatus.values())
                .collect(
                        Collectors.toMap(
                                documentStatus -> documentStatus,
                                documentStatus -> statusMap.getOrDefault(documentStatus, 0), (a, b) -> b,
                                TreeMap::new
                        )
                )
        );
    }
}
