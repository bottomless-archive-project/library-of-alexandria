package com.github.loa.vault.view.response.domain;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class FreeSpaceResponse {

    private final long freeSpace;
}
