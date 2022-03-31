package com.github.bottomlessarchive.loa.web.view.document.controller;

import com.github.bottomlessarchive.loa.web.view.document.response.dashboard.DashboardDocumentStatisticsResponse;
import com.github.bottomlessarchive.loa.web.view.document.response.dashboard.DashboardServicesResponse;
import com.github.bottomlessarchive.loa.web.view.document.service.DocumentStatisticsResponseFactory;
import com.github.bottomlessarchive.loa.web.view.document.service.ServicesResponseFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/dashboard")
@RequiredArgsConstructor
public class DashboardController {

    private final ServicesResponseFactory servicesResponseFactory;
    private final DocumentStatisticsResponseFactory documentStatisticsResponseFactory;

    @GetMapping("/statistics")
    public DashboardDocumentStatisticsResponse documentStatistics() {
        return documentStatisticsResponseFactory.newStatisticsResponse();
    }

    @GetMapping("/services")
    public DashboardServicesResponse services() {
        return servicesResponseFactory.newServicesResponse();
    }
}
