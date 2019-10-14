package com.github.loa.indexer.service.search.domain;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum DocumentLength {

    SHORT_STORY(1, 10),
    NOVELETTE(11, 50),
    NOVELLA(51, 150),
    NOVEL(151, Integer.MAX_VALUE);

    private final int minimumPageCount;
    private final int maximumPageCount;
}
