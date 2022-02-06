package com.github.bottomlessarchive.loa.vault.client.service.domain;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class VaultLocation {

    private final String location;
    private final int port;
}
