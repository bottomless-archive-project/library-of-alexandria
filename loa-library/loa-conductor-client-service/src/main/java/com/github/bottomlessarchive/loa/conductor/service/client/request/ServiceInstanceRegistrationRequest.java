package com.github.bottomlessarchive.loa.conductor.service.client.request;

import lombok.Builder;
import lombok.Value;
import lombok.extern.jackson.Jacksonized;

@Value
@Builder
@Jacksonized
public class ServiceInstanceRegistrationRequest {

    String location;
    int port;
}
