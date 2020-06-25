package com.github.loa.statistics.service.entity;

import com.github.loa.statistics.repository.StatisticsRepository;
import com.github.loa.statistics.repository.domain.StatisticsDatabaseEntity;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.time.Instant;

@Service
@RequiredArgsConstructor
public class StatisticsEntityFactory {

    private final StatisticsRepository statisticsRepository;

    public Mono<Void> createStatisticsPoint(final long documentCount, final long documentLocationCount) {
        final StatisticsDatabaseEntity statisticsDatabaseEntity = new StatisticsDatabaseEntity();

        statisticsDatabaseEntity.setCreatedAt(Instant.now());
        statisticsDatabaseEntity.setDocumentCount(documentCount);
        statisticsDatabaseEntity.setDocumentLocationCount(documentLocationCount);

        return statisticsRepository.insertStatistics(statisticsDatabaseEntity);
    }
}
