package com.github.bottomlessarchive.loa.conductor.service.client.request;

import lombok.Builder;
import lombok.Value;
import lombok.extern.jackson.Jacksonized;

import java.util.List;
import java.util.Map;

@Value
@Builder
@Jacksonized
public class ServiceInstanceRegistrationRequest {

    String location;
    int port;
    List<ServiceInstanceRegistrationPropertyRequest> properties;
}
