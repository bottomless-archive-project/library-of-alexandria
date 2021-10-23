package com.github.loa.logging.service;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.Meter;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class LoggingMeterRegistry extends SimpleMeterRegistry {

    private final MetricLogger metricLogger;

    public LoggingMeterRegistry(final MetricLogger metricLogger) {
        this.metricLogger = metricLogger;
    }

    @Scheduled(fixedDelay = 6000)
    public void printMetrics() {
        getMeters().forEach(meter -> {
            if (meter.getId().getType() == Meter.Type.COUNTER) {
                metricLogger.logCounter((Counter) meter);
            }
        });
    }
}
