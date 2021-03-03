package com.github.loa.vault.view.response.domain;

import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class QueryDocumentResponse {

    byte[] payload;
}
