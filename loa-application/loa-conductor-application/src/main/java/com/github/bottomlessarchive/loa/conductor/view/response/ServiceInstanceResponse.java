package com.github.bottomlessarchive.loa.conductor.view.response;

import lombok.Builder;
import lombok.Getter;

import java.time.Instant;

@Getter
@Builder
public class ServiceInstanceResponse {

    private final String location;
    private final int port;
    private final Instant lastHeartbeat;
}
