package com.github.bottomlessarchive.loa.location.domain;

import com.github.bottomlessarchive.loa.location.domain.link.Link;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class DocumentLocation {

    private final Link location;
    private final String sourceName;
}
