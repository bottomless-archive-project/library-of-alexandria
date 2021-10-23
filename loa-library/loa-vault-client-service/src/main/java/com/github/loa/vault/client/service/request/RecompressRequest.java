package com.github.loa.vault.client.service.request;

import com.github.loa.compression.domain.DocumentCompression;
import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class RecompressRequest {

    String documentId;
    DocumentCompression compression;
}
