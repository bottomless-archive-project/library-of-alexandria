package com.github.loa.source.commoncrawl.service.webpage.domain;

import lombok.Builder;
import lombok.Getter;

/**
 * Holds the data of a web download response.
 */
@Getter
@Builder
public class WebPage {

    private final String url;
    private final String content;
}
