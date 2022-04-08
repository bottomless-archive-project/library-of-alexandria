package com.github.bottomlessarchive.loa.web.view.document.response.dashboard;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class DownloaderServiceInstance {

    private final String host;
    private final int port;
}
