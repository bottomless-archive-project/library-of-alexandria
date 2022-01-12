package com.github.bottomlessarchive.loa.conductor.view.request;

import lombok.Builder;
import lombok.Getter;
import lombok.extern.jackson.Jacksonized;

@Getter
@Builder
@Jacksonized
public class ServiceInstancePropertyRequest {

    private final String name;
    private final String value;
}
