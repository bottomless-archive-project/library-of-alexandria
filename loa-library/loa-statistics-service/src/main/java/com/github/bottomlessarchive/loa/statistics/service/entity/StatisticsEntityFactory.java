package com.github.bottomlessarchive.loa.statistics.service.entity;

import com.github.bottomlessarchive.loa.statistics.repository.StatisticsRepository;
import com.github.bottomlessarchive.loa.statistics.repository.domain.StatisticsDatabaseEntity;
import com.github.bottomlessarchive.loa.statistics.service.entity.domain.StatisticsEntity;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.time.Instant;

@Service
@RequiredArgsConstructor
public class StatisticsEntityFactory {

    private final StatisticsRepository statisticsRepository;

    public Flux<StatisticsEntity> getStatisticsBetween(final Duration duration) {
        return statisticsRepository.findStatisticsBetween(duration)
                .map(statisticsDatabaseEntity -> StatisticsEntity.builder()
                        .createdAt(statisticsDatabaseEntity.getCreatedAt())
                        .documentCount(statisticsDatabaseEntity.getDocumentCount())
                        .documentLocationCount(statisticsDatabaseEntity.getDocumentLocationCount())
                        .build()
                );
    }

    public Mono<Void> createStatisticsPoint(final long documentCount, final long documentLocationCount) {
        final StatisticsDatabaseEntity statisticsDatabaseEntity = new StatisticsDatabaseEntity();

        statisticsDatabaseEntity.setCreatedAt(Instant.now());
        statisticsDatabaseEntity.setDocumentCount(documentCount);
        statisticsDatabaseEntity.setDocumentLocationCount(documentLocationCount);

        return statisticsRepository.insertStatistics(statisticsDatabaseEntity);
    }
}
