package com.github.bottomlessarchive.loa.conductor.service.domain;

import lombok.Builder;
import lombok.Getter;

import java.time.Instant;
import java.util.UUID;

@Getter
@Builder
public class ServiceInstanceEntity {

    private final UUID id;
    private final String name;
    private final String location;
    private final int port;

    private Instant lastHeartbeat;

    public void refreshHeartbeat() {
        lastHeartbeat = Instant.now();
    }
}
