package com.github.bottomlessarchive.loa.conductor.service.domain;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class ServiceInstanceEntityProperty {

    private final String name;
    private final String value;
}
