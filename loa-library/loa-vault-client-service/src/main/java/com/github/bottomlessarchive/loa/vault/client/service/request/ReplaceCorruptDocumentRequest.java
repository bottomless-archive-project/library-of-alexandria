package com.github.bottomlessarchive.loa.vault.client.service.request;

import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class ReplaceCorruptDocumentRequest {

    byte[] content;
}
