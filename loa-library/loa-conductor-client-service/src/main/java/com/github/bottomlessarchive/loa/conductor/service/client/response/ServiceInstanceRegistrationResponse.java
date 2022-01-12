package com.github.bottomlessarchive.loa.conductor.service.client.response;

import lombok.Builder;
import lombok.Value;
import lombok.extern.jackson.Jacksonized;

import java.util.UUID;

@Value
@Builder
@Jacksonized
public class ServiceInstanceRegistrationResponse {

    UUID id;
}
