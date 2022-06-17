package com.github.bottomlessarchive.loa.web.view.dashboard.response;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class IndexerApplicationInstance {

    private final String host;
    private final int port;
    private final int parallelism;
    private final int batchSize;
}
