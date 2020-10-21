package com.github.loa.vault.client.service.request;

import com.github.loa.compression.domain.DocumentCompression;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class RecompressRequest {

    private final DocumentCompression compression;
}
