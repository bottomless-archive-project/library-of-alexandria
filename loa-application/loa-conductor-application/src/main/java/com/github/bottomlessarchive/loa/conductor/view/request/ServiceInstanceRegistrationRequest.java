package com.github.bottomlessarchive.loa.conductor.view.request;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class ServiceInstanceRegistrationRequest {

    private final String location;
    private final int port;
}
