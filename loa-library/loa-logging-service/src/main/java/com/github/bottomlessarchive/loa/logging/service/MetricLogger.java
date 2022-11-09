package com.github.bottomlessarchive.loa.logging.service;

import io.micrometer.core.instrument.Counter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Arrays;

@Slf4j
@Service
public class MetricLogger {

    public void logCounters(final Counter... counter) {
        Arrays.stream(counter)
                .forEach(this::logCounter);
    }

    public void logCounter(final Counter counter) {
        if (log.isInfoEnabled()) {
            log.info("The {} are {} {}!", counter.getId().getTag("printed-name"), (int) counter.count(),
                    counter.getId().getBaseUnit());
        }
    }
}
