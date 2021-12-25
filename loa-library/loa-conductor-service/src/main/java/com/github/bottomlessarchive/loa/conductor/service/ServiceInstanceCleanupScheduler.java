package com.github.bottomlessarchive.loa.conductor.service;

import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
public class ServiceInstanceCleanupScheduler {

    private final ServiceContainer serviceContainer;

    @Scheduled(fixedRate = 2, timeUnit = TimeUnit.MINUTES)
    public void cleanupServiceInstances() {
        serviceContainer.cleanupServiceInstances();
    }
}
