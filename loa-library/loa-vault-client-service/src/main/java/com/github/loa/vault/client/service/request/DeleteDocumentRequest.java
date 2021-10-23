package com.github.loa.vault.client.service.request;

import lombok.Builder;
import lombok.Value;
import lombok.extern.jackson.Jacksonized;

@Value
@Builder
@Jacksonized
public class DeleteDocumentRequest {

    String documentId;
}
