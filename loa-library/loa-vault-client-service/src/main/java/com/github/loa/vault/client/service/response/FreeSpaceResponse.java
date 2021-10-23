package com.github.loa.vault.client.service.response;

import lombok.Builder;
import lombok.Value;
import lombok.extern.jackson.Jacksonized;

@Value
@Builder
@Jacksonized
public class FreeSpaceResponse {

    long freeSpace;
}
