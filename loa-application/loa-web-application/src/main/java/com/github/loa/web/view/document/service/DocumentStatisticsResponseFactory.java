package com.github.loa.web.view.document.service;

import com.github.loa.document.service.domain.DocumentStatus;
import com.github.loa.document.service.domain.DocumentType;
import com.github.loa.document.service.entity.factory.DocumentEntityFactory;
import com.github.loa.vault.client.configuration.VaultClientConfigurationProperties;
import com.github.loa.vault.client.service.VaultClientService;
import com.github.loa.web.view.document.response.dashboard.DashboardDocumentStatisticsResponse;
import com.github.loa.web.view.document.response.dashboard.DashboardVaultStatisticsResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Arrays;
import java.util.Map;
import java.util.TreeMap;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class DocumentStatisticsResponseFactory {

    private final VaultClientService vaultClientService;
    private final DocumentEntityFactory documentEntityFactory;
    private final VaultClientConfigurationProperties vaultClientConfigurationProperties;

    public Mono<DashboardDocumentStatisticsResponse> newStatisticsResponse() {
        return documentEntityFactory.getDocumentCount()
                .map(count -> DashboardDocumentStatisticsResponse.builder()
                        .documentCount(count)
                )
                .flatMap(this::fillDocumentCountByType)
                .flatMap(this::fillDocumentCountByStatus)
                .flatMap(this::fillVaultInstances)
                .map(DashboardDocumentStatisticsResponse.DashboardDocumentStatisticsResponseBuilder::build);
    }

    private Mono<DashboardDocumentStatisticsResponse.DashboardDocumentStatisticsResponseBuilder> fillVaultInstances(
            final DashboardDocumentStatisticsResponse.DashboardDocumentStatisticsResponseBuilder builder) {
        return Mono.just(vaultClientConfigurationProperties.getLocations().keySet())
                .flatMap(vaultNames -> Flux.fromIterable(vaultNames)
                        .flatMap(vaultName -> vaultClientService.getAvailableSpace(vaultName)
                                .map(availableSpace -> DashboardVaultStatisticsResponse.builder()
                                        .name(vaultName)
                                        .availableStorageInBytes(availableSpace)
                                        .build()
                                )
                        )
                        .collect(Collectors.toSet())
                )
                .map(builder::vaultInstances);
    }

    private Mono<DashboardDocumentStatisticsResponse.DashboardDocumentStatisticsResponseBuilder> fillDocumentCountByType(
            final DashboardDocumentStatisticsResponse.DashboardDocumentStatisticsResponseBuilder builder) {
        return documentEntityFactory.getCountByType()
                .map(typeMap -> Arrays.stream(DocumentType.values())
                        .collect(Collectors.toMap(documentType -> documentType, documentType -> typeMap.getOrDefault(documentType, 0), (a, b) -> b, TreeMap::new)))
                .map(builder::documentCountByType);
    }

    private Mono<DashboardDocumentStatisticsResponse.DashboardDocumentStatisticsResponseBuilder> fillDocumentCountByStatus(
            final DashboardDocumentStatisticsResponse.DashboardDocumentStatisticsResponseBuilder builder) {
        return documentEntityFactory.getCountByStatus()
                .map(statusMap -> Arrays.stream(DocumentStatus.values())
                        .collect(Collectors.toMap(documentStatus -> documentStatus, documentStatus -> statusMap.getOrDefault(documentStatus, 0), (a, b) -> b, TreeMap::new)))
                .map(builder::documentCountByStatus);
    }
}
