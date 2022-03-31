package com.github.bottomlessarchive.loa.web.view.document.response.dashboard;

import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public class DashboardServicesResponse {

    private final List<QueueServiceInstance> queues;
}
