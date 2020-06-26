package com.github.loa.statistics.repository;

import com.github.loa.statistics.repository.domain.StatisticsDatabaseEntity;
import com.mongodb.reactivestreams.client.MongoCollection;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.time.Instant;

import static com.mongodb.client.model.Filters.*;

@Component
@RequiredArgsConstructor
public class StatisticsRepository {

    private final MongoCollection<StatisticsDatabaseEntity> statisticsDatabaseEntityMongoCollection;

    public Flux<StatisticsDatabaseEntity> findStatisticsBetween(final Duration duration) {
        return Flux.from(statisticsDatabaseEntityMongoCollection.find(and(gte("createdAt", Instant.now().minus(duration)),
                lt("createdAScht", Instant.now()))));
    }

    public Mono<Void> insertStatistics(final StatisticsDatabaseEntity statisticsDatabaseEntity) {
        return Mono.from(statisticsDatabaseEntityMongoCollection.insertOne(statisticsDatabaseEntity))
                .then();
    }
}
