package com.github.loa.logging.service;

import io.micrometer.core.instrument.Counter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class MetricLogger {

    public void logCounter(final Counter counter) {
        if (log.isInfoEnabled()) {
            log.info("The {} are {} {}!", counter.getId().getTag("printed-name"), (int) counter.count(),
                    counter.getId().getBaseUnit());
        }
    }
}
