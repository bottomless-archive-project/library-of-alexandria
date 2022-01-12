package com.github.bottomlessarchive.loa.conductor.service.domain;

import lombok.Builder;
import lombok.Getter;

import java.util.Collections;
import java.util.List;

@Getter
@Builder
public class ServiceInstanceRegistrationContext {

    private final String location;
    private final int port;
    private final List<ServiceInstanceRegistrationProperty> properties;

    public List<ServiceInstanceRegistrationProperty> getProperties() {
        return Collections.unmodifiableList(properties);
    }
}
