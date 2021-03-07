package com.github.loa.vault.view.request.domain;

import com.github.loa.compression.domain.DocumentCompression;
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
