package com.github.bottomlessarchive.loa.web.view.document.response.dashboard;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class IndexerServiceInstance {

    private final String host;
    private final int port;
}
