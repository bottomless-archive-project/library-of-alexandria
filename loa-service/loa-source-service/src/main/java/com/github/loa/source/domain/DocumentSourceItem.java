package com.github.loa.source.domain;

import lombok.Builder;
import lombok.Getter;

import java.net.URL;

@Getter
@Builder
public class DocumentSourceItem {

    private final String sourceName;
    private final URL documentLocation;
}
