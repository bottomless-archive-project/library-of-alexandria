package com.github.bottomlessarchive.loa.conductor.view.request;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.Collections;
import java.util.List;

@Getter
@RequiredArgsConstructor
public class ServiceInstanceRegistrationRequest {

    private final String location;
    private final int port;
    private final List<ServiceInstanceRegistrationPropertyRequest> properties;

    public List<ServiceInstanceRegistrationPropertyRequest> getProperties() {
        return Collections.unmodifiableList(properties);
    }
}
