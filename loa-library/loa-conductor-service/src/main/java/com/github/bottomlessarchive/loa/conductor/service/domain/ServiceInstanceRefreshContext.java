package com.github.bottomlessarchive.loa.conductor.service.domain;

import com.github.bottomlessarchive.loa.application.domain.ApplicationType;
import lombok.Builder;
import lombok.Getter;

import java.util.List;
import java.util.UUID;

@Getter
@Builder
public class ServiceInstanceRefreshContext {

    private final UUID instanceId;
    private final ApplicationType applicationType;
    private final List<ServiceInstanceProperty> properties;
}
