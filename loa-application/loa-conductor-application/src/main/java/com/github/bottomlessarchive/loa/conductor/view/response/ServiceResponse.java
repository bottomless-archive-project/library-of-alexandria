package com.github.bottomlessarchive.loa.conductor.view.response;

import com.github.bottomlessarchive.loa.application.domain.ApplicationType;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public class ServiceResponse {

    private final ApplicationType applicationType;
    private final List<ServiceInstanceResponse> instances;
}
