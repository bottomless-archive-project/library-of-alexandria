package com.github.bottomlessarchive.loa.vault.view.request.domain;

import com.github.bottomlessarchive.loa.compression.domain.DocumentCompression;
import lombok.Builder;
import lombok.Value;
import lombok.extern.jackson.Jacksonized;

@Value
@Builder
@Jacksonized
public class RecompressDocumentRequest {

    String documentId;
    DocumentCompression compression;
}
