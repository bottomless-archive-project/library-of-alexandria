package com.github.bottomlessarchive.loa.conductor.view.response;

import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public class ServiceResponse {

    private final String name;
    private final List<ServiceInstanceResponse> instances;
}
