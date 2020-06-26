package com.github.loa.web.view.document.response.dashboard;

import lombok.Builder;
import lombok.Getter;

import java.time.Instant;

@Getter
@Builder
public class DashboardStatisticsResponse {

    private final Instant createdAt;
    private final long documentCount;
    private final long documentLocationCount;
}
