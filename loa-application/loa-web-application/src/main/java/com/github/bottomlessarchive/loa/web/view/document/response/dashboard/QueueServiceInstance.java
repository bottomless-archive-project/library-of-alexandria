package com.github.bottomlessarchive.loa.web.view.document.response.dashboard;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class QueueServiceInstance {

    private final long documentLocationQueueCount;
    private final long documentArchivingQueueCount;
}
