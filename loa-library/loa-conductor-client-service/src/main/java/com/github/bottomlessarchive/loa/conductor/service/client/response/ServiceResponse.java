package com.github.bottomlessarchive.loa.conductor.service.client.response;

import com.github.bottomlessarchive.loa.application.domain.ApplicationType;
import lombok.Builder;
import lombok.Getter;
import lombok.extern.jackson.Jacksonized;

import java.util.List;

@Getter
@Builder
@Jacksonized
public class ServiceResponse {

    private final ApplicationType applicationType;
    private final List<ServiceInstanceResponse> instances;
}
