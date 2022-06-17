package com.github.bottomlessarchive.loa.web.view.dashboard.controller;

import com.github.bottomlessarchive.loa.web.view.dashboard.response.DashboardDocumentStatisticsResponse;
import com.github.bottomlessarchive.loa.web.view.dashboard.response.DashboardApplicationsResponse;
import com.github.bottomlessarchive.loa.web.view.dashboard.service.DocumentStatisticsResponseFactory;
import com.github.bottomlessarchive.loa.web.view.dashboard.service.ApplicationStatisticsResponseFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/dashboard")
@RequiredArgsConstructor
public class DashboardController {

    private final ApplicationStatisticsResponseFactory applicationStatisticsResponseFactory;
    private final DocumentStatisticsResponseFactory documentStatisticsResponseFactory;

    @GetMapping("/statistics")
    public DashboardDocumentStatisticsResponse documentStatistics() {
        return documentStatisticsResponseFactory.newStatisticsResponse();
    }

    @GetMapping("/applications")
    public DashboardApplicationsResponse services() {
        return applicationStatisticsResponseFactory.newApplicationsResponse();
    }
}
