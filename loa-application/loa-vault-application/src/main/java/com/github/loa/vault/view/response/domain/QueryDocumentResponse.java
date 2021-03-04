package com.github.loa.vault.view.response.domain;

import lombok.Builder;
import lombok.Value;
import lombok.extern.jackson.Jacksonized;

@Value
@Builder
@Jacksonized
public class QueryDocumentResponse {

    byte[] payload;
}
