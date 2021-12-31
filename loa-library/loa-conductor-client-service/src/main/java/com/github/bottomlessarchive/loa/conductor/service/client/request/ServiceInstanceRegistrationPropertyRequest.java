package com.github.bottomlessarchive.loa.conductor.service.client.request;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class ServiceInstanceRegistrationPropertyRequest {

    private final String name;
    private final String value;
}
