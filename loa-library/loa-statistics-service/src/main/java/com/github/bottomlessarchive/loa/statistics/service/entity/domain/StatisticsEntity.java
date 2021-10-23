package com.github.bottomlessarchive.loa.statistics.service.entity.domain;

import lombok.Builder;
import lombok.Getter;

import java.time.Instant;

@Getter
@Builder
public class StatisticsEntity {

    private final Instant createdAt;
    private final long documentCount;
    private final long documentLocationCount;
}
