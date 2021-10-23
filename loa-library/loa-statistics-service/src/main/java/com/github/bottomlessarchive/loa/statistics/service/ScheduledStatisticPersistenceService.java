package com.github.bottomlessarchive.loa.statistics.service;

import com.github.bottomlessarchive.loa.document.service.entity.factory.DocumentEntityFactory;
import com.github.bottomlessarchive.loa.location.service.factory.DocumentLocationEntityFactory;
import com.github.bottomlessarchive.loa.statistics.service.entity.StatisticsEntityFactory;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Slf4j
@Service
@RequiredArgsConstructor
public class ScheduledStatisticPersistenceService {

    private final StatisticsEntityFactory statisticsEntityFactory;
    private final DocumentEntityFactory documentEntityFactory;
    private final DocumentLocationEntityFactory documentLocationEntityFactory;

    @Scheduled(fixedRateString = "${loa.statistics.collection-rate}")
    public void persistStatistics() {
        log.info("Persisting statistics.");

        Mono.zip(documentEntityFactory.getDocumentCount(), documentLocationEntityFactory.getDocumentLocationCount())
                .flatMap(count -> statisticsEntityFactory.createStatisticsPoint(count.getT1(), count.getT2()))
                .subscribe();
    }
}
