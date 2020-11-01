package com.github.loa.web.view.document.service;

import com.github.loa.document.service.domain.DocumentStatus;
import com.github.loa.document.service.domain.DocumentType;
import com.github.loa.document.service.entity.factory.DocumentEntityFactory;
import com.github.loa.queue.service.QueueManipulator;
import com.github.loa.queue.service.domain.Queue;
import com.github.loa.statistics.service.entity.StatisticsEntityFactory;
import com.github.loa.vault.client.configuration.VaultClientConfigurationProperties;
import com.github.loa.vault.client.service.VaultClientService;
import com.github.loa.web.view.document.response.dashboard.DashboardDocumentStatisticsResponse;
import com.github.loa.web.view.document.response.dashboard.DashboardQueueStatisticsResponse;
import com.github.loa.web.view.document.response.dashboard.DashboardStatisticsResponse;
import com.github.loa.web.view.document.response.dashboard.DashboardVaultStatisticsResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.Arrays;
import java.util.List;
import java.util.TreeMap;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class DocumentStatisticsResponseFactory {

    private final QueueManipulator queueManipulator;
    private final VaultClientService vaultClientService;
    private final DocumentEntityFactory documentEntityFactory;
    private final StatisticsEntityFactory statisticsEntityFactory;
    private final VaultClientConfigurationProperties vaultClientConfigurationProperties;

    public Mono<DashboardDocumentStatisticsResponse> newStatisticsResponse() {
        return documentEntityFactory.getDocumentCount()
                .map(count -> DashboardDocumentStatisticsResponse.builder()
                        .documentCount(count)
                )
                .flatMap(this::fillDocumentCountByType)
                .flatMap(this::fillDocumentCountByStatus)
                .flatMap(this::fillVaultInstances)
                .flatMap(this::fillQueues)
                .flatMap(this::fillStatistics)
                .map(DashboardDocumentStatisticsResponse.DashboardDocumentStatisticsResponseBuilder::build);
    }

    private Mono<DashboardDocumentStatisticsResponse.DashboardDocumentStatisticsResponseBuilder> fillStatistics(
            final DashboardDocumentStatisticsResponse.DashboardDocumentStatisticsResponseBuilder builder) {
        return statisticsEntityFactory.getStatisticsBetween(Duration.ofDays(1))
                .map(statisticsEntity -> DashboardStatisticsResponse.builder()
                        .createdAt(statisticsEntity.getCreatedAt())
                        .documentCount(statisticsEntity.getDocumentCount())
                        .documentLocationCount(statisticsEntity.getDocumentLocationCount())
                        .build()
                )
                .collectList()
                .map(builder::statistics);
    }

    private Mono<DashboardDocumentStatisticsResponse.DashboardDocumentStatisticsResponseBuilder> fillQueues(
            final DashboardDocumentStatisticsResponse.DashboardDocumentStatisticsResponseBuilder builder) {
        final List<DashboardQueueStatisticsResponse> dashboardQueueStatisticsResponses = Arrays.stream(Queue.values())
                .map(queue -> DashboardQueueStatisticsResponse.builder()
                        .name(queue.name())
                        .messageCount(queueManipulator.getMessageCount(queue))
                        .build()
                )
                .collect(Collectors.toList());

        return Mono.just(builder.queues(dashboardQueueStatisticsResponses));
    }

    private Mono<DashboardDocumentStatisticsResponse.DashboardDocumentStatisticsResponseBuilder> fillVaultInstances(
            final DashboardDocumentStatisticsResponse.DashboardDocumentStatisticsResponseBuilder builder) {
        return Mono.just(vaultClientConfigurationProperties.getLocations().keySet())
                .flatMap(vaultNames -> Flux.fromIterable(vaultNames)
                        .flatMap(vaultName -> vaultClientService.getAvailableSpace(vaultName)
                                .onErrorReturn(-1L)
                                .map(availableSpace -> DashboardVaultStatisticsResponse.builder()
                                        .name(vaultName)
                                        .availableStorageInBytes(availableSpace)
                                        .build()
                                )
                        )
                        .collect(Collectors.toList())
                )
                .map(builder::vaultInstances);
    }

    private Mono<DashboardDocumentStatisticsResponse.DashboardDocumentStatisticsResponseBuilder> fillDocumentCountByType(
            final DashboardDocumentStatisticsResponse.DashboardDocumentStatisticsResponseBuilder builder) {
        return documentEntityFactory.getCountByType()
                .map(typeMap -> Arrays.stream(DocumentType.values())
                        .collect(Collectors.toMap(documentType -> documentType,
                                documentType -> typeMap.getOrDefault(documentType, 0), (a, b) -> b, TreeMap::new)))
                .map(builder::documentCountByType);
    }

    private Mono<DashboardDocumentStatisticsResponse.DashboardDocumentStatisticsResponseBuilder> fillDocumentCountByStatus(
            final DashboardDocumentStatisticsResponse.DashboardDocumentStatisticsResponseBuilder builder) {
        return documentEntityFactory.getCountByStatus()
                .map(statusMap -> Arrays.stream(DocumentStatus.values())
                        .collect(Collectors.toMap(documentStatus -> documentStatus,
                                documentStatus -> statusMap.getOrDefault(documentStatus, 0), (a, b) -> b, TreeMap::new)))
                .map(builder::documentCountByStatus);
    }
}
