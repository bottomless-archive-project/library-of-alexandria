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

    @Builder.Default
    private final List<ServiceInstanceProperty> properties = Collections.emptyList();

    public List<ServiceInstanceProperty> getProperties() {
        return Collections.unmodifiableList(properties);
    }
}
