package com.github.loa.statistics.repository;

import com.github.loa.statistics.repository.domain.StatisticsDatabaseEntity;
import com.mongodb.reactivestreams.client.MongoCollection;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

@Component
@RequiredArgsConstructor
public class StatisticsRepository {

    private final MongoCollection<StatisticsDatabaseEntity> statisticsDatabaseEntityMongoCollection;

    public Mono<Void> insertStatistics(final StatisticsDatabaseEntity statisticsDatabaseEntity) {
        return Mono.from(statisticsDatabaseEntityMongoCollection.insertOne(statisticsDatabaseEntity))
                .then();
    }
}
