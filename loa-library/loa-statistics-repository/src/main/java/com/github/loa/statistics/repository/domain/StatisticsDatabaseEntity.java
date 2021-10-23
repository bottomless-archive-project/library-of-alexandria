package com.github.loa.statistics.repository.domain;

import lombok.Getter;
import lombok.Setter;

import java.time.Instant;

@Getter
@Setter
public class StatisticsDatabaseEntity {

    private Instant createdAt;
    private long documentCount;
    private long documentLocationCount;
}
