package com.github.loa.vault.view.request.domain;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;
import com.github.loa.compression.domain.DocumentCompression;
import lombok.Builder;
import lombok.Value;

@Value
@Builder(builderClassName = "LombokBuilder")
@JsonDeserialize(builder = RecompressRequest.LombokBuilder.class)
public class RecompressRequest {

    DocumentCompression compression;

    @JsonPOJOBuilder(withPrefix = "")
    public static class LombokBuilder {
    }
}
