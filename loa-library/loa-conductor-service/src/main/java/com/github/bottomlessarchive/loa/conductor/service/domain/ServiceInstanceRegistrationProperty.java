package com.github.bottomlessarchive.loa.conductor.service.domain;

import lombok.Builder;
import lombok.Getter;
import lombok.extern.jackson.Jacksonized;

@Getter
@Builder
@Jacksonized
public class ServiceInstanceRegistrationProperty {

    private final String name;
    private final String value;
}
