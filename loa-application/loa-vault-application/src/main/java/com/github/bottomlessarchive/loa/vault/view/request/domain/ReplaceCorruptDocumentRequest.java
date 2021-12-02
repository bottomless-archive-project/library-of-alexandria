package com.github.bottomlessarchive.loa.vault.view.request.domain;

import lombok.Builder;
import lombok.Value;
import lombok.extern.jackson.Jacksonized;

@Value
@Builder
@Jacksonized
public class ReplaceCorruptDocumentRequest {

    String documentId;
    byte[] content;
}
