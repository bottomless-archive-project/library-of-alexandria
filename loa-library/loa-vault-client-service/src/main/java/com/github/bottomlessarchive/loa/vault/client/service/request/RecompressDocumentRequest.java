package com.github.bottomlessarchive.loa.vault.client.service.request;

import com.github.bottomlessarchive.loa.compression.domain.DocumentCompression;
import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class RecompressDocumentRequest {

    DocumentCompression compression;
}
