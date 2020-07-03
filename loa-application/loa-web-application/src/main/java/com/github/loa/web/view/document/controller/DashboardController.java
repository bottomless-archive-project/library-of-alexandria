package com.github.loa.web.view.document.controller;

import com.github.loa.web.view.document.response.dashboard.DashboardDocumentStatisticsResponse;
import com.github.loa.web.view.document.service.DocumentStatisticsResponseFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/dashboard")
@RequiredArgsConstructor
public class DashboardController {

    private final DocumentStatisticsResponseFactory documentStatisticsResponseFactory;

    @GetMapping("/statistics")
    public Mono<DashboardDocumentStatisticsResponse> documentStatistics() {
        return documentStatisticsResponseFactory.newStatisticsResponse();
    }
}
