package com.github.loa.web.view.document.service;

import com.github.loa.document.service.domain.DocumentStatus;
import com.github.loa.document.service.domain.DocumentType;
import com.github.loa.document.service.entity.factory.DocumentEntityFactory;
import com.github.loa.web.view.document.response.DashboardDocumentStatisticsResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.util.Arrays;
import java.util.TreeMap;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class DocumentStatisticsResponseFactory {

    private final DocumentEntityFactory documentEntityFactory;

    public Mono<DashboardDocumentStatisticsResponse> newStatisticsResponse() {
        return documentEntityFactory.getDocumentCount()
                .map(count -> DashboardDocumentStatisticsResponse.builder()
                        .documentCount(count)
                )
                .flatMap(this::fillDocumentCountByType)
                .flatMap(this::fillDocumentCountByStatus)
                .map(DashboardDocumentStatisticsResponse.DashboardDocumentStatisticsResponseBuilder::build);
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
