package com.github.loa.location.domain;

import lombok.Builder;
import lombok.Getter;

import java.net.URL;

@Getter
@Builder
public class DocumentLocation {

    private final URL location;
    private final String sourceName;
}
