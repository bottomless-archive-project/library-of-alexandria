package com.github.bottomlessarchive.loa.web.view.document.response.dashboard;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class DashboardQueueStatisticsResponse {

    private final String name;
    private final long messageCount;
}
