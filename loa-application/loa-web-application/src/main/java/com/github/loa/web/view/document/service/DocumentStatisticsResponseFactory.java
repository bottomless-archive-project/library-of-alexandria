package com.github.loa.web.view.document.service;

import com.github.loa.document.service.entity.factory.DocumentEntityFactory;
import com.github.loa.web.view.document.response.DashboardDocumentStatisticsResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

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
                .map(DashboardDocumentStatisticsResponse.DashboardDocumentStatisticsResponseBuilder::build);
    }

    private Mono<DashboardDocumentStatisticsResponse.DashboardDocumentStatisticsResponseBuilder> fillDocumentCountByType(
            final DashboardDocumentStatisticsResponse.DashboardDocumentStatisticsResponseBuilder builder) {
        return documentEntityFactory.getCountByStatus()
                .map(builder::documentCountByStatus);
    }
}
