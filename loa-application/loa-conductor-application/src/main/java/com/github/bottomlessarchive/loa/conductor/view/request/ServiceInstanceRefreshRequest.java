package com.github.bottomlessarchive.loa.conductor.view.request;

import lombok.Builder;
import lombok.RequiredArgsConstructor;
import lombok.extern.jackson.Jacksonized;

import java.util.Collections;
import java.util.List;

@Builder
@Jacksonized
@RequiredArgsConstructor
public class ServiceInstanceRefreshRequest {

    private final List<ServiceInstancePropertyRequest> properties;

    public List<ServiceInstancePropertyRequest> getProperties() {
        return Collections.unmodifiableList(properties);
    }
}
