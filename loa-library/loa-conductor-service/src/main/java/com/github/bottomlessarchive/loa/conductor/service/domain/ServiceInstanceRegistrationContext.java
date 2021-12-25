package com.github.bottomlessarchive.loa.conductor.service.domain;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class ServiceInstanceRegistrationContext {

    private final String location;
    private final int port;
}
