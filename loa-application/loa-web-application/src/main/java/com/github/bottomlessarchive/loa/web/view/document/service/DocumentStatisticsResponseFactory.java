package com.github.bottomlessarchive.loa.web.view.document.service;

import com.github.bottomlessarchive.loa.application.domain.ApplicationType;
import com.github.bottomlessarchive.loa.conductor.service.client.ConductorClient;
import com.github.bottomlessarchive.loa.conductor.service.domain.ServiceInstanceEntityProperty;
import com.github.bottomlessarchive.loa.document.service.domain.DocumentStatus;
import com.github.bottomlessarchive.loa.document.service.domain.DocumentType;
import com.github.bottomlessarchive.loa.document.service.entity.factory.DocumentEntityFactory;
import com.github.bottomlessarchive.loa.queue.service.QueueManipulator;
import com.github.bottomlessarchive.loa.web.view.document.response.dashboard.DashboardDocumentStatisticsResponse;
import com.github.bottomlessarchive.loa.web.view.document.response.dashboard.DashboardQueueStatisticsResponse;
import com.github.bottomlessarchive.loa.web.view.document.response.dashboard.DashboardVaultStatisticsResponse;
import com.github.bottomlessarchive.loa.queue.service.domain.Queue;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class DocumentStatisticsResponseFactory {

    private final ConductorClient conductorClient;
    private final QueueManipulator queueManipulator;
    private final DocumentEntityFactory documentEntityFactory;

    public DashboardDocumentStatisticsResponse newStatisticsResponse() {
        final DashboardDocumentStatisticsResponse.DashboardDocumentStatisticsResponseBuilder builder =
                DashboardDocumentStatisticsResponse.builder();

        builder.documentCount(documentEntityFactory.getEstimatedDocumentCount());
        builder.queues(Arrays.stream(Queue.values())
                .map(queue -> DashboardQueueStatisticsResponse.builder()
                        .name(queue.name())
                        .messageCount(queueManipulator.getMessageCount(queue))
                        .build()
                )
                .toList()
        );

        fillDocumentCountByStatus(builder);
        fillDocumentCountByType(builder);
        fillVaultInstances(builder);

        return builder.build();
    }

    private void fillVaultInstances(
            final DashboardDocumentStatisticsResponse.DashboardDocumentStatisticsResponseBuilder builder) {

        final List<DashboardVaultStatisticsResponse> serviceInstanceEntityList =
                conductorClient.getInstances(ApplicationType.VAULT_APPLICATION)
                        .toStream()
                        .map(serviceInstanceEntity -> DashboardVaultStatisticsResponse.builder()
                                .name(serviceInstanceEntity.getProperty("name")
                                        .map(ServiceInstanceEntityProperty::getValue)
                                        .orElse("Unknown Vault!")
                                )
                                .availableStorageInBytes(
                                        serviceInstanceEntity.getProperty("freeSpace")
                                                .map(ServiceInstanceEntityProperty::getValue)
                                                .map(Long::parseLong)
                                                .orElse(-1L)
                                )
                                .build()
                        )
                        .toList();

        builder.vaultInstances(serviceInstanceEntityList);
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
