package com.github.bottomlessarchive.loa.conductor.service.client.response;

import lombok.Builder;
import lombok.Getter;

import java.time.Instant;
import java.util.UUID;

@Getter
@Builder
public class ServiceInstanceResponse {

    private final UUID id;
    private final String location;
    private final int port;
    private final Instant lastHeartbeat;
}
