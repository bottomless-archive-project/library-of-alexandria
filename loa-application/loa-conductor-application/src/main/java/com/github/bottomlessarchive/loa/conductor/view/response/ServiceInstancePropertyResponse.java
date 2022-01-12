package com.github.bottomlessarchive.loa.conductor.view.response;

import lombok.Builder;
import lombok.Getter;
import lombok.extern.jackson.Jacksonized;

@Getter
@Builder
@Jacksonized
public class ServiceInstancePropertyResponse {

    private final String name;
    private final String value;
}
