package com.github.loa.source.commoncrawl.domain;

import lombok.Builder;
import lombok.Getter;

import java.net.URL;

@Getter
@Builder
public class CommonCrawlWarcLocation {

    private final int id;
    private final URL location;
}
