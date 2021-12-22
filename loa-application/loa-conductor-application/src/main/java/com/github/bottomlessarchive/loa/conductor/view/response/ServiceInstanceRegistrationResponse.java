package com.github.bottomlessarchive.loa.conductor.view.response;

import lombok.Builder;
import lombok.Getter;

import java.util.UUID;

@Getter
@Builder
public class ServiceInstanceRegistrationResponse {

    private final UUID id;
}
