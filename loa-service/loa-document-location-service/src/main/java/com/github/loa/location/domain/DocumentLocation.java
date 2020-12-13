package com.github.loa.location.domain;

import com.github.loa.location.domain.link.Link;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class DocumentLocation {

    private final Link location;
    private final String sourceName;
}
