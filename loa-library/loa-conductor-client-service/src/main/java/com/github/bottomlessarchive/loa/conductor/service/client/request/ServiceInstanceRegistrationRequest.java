package com.github.bottomlessarchive.loa.conductor.service.client.request;

import lombok.Builder;
import lombok.Value;
import lombok.extern.jackson.Jacksonized;

import java.util.List;

@Value
@Builder
@Jacksonized
public class ServiceInstanceRegistrationRequest {

    String location;
    int port;
    List<ServiceInstanceRegistrationPropertyRequest> properties;
}
