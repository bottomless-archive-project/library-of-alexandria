package com.github.bottomlessarchive.loa.web.view.dashboard.response;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class GeneratorApplicationInstance {

    private final String host;
    private final int port;
}
