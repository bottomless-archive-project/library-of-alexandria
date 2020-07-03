package com.github.loa.vault.client.service.response;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;
import com.github.loa.vault.client.service.response.FreeSpaceResponse.LombokBuilder;
import lombok.Builder;
import lombok.Value;

@Value
@Builder(builderClassName = "LombokBuilder")
@JsonDeserialize(builder = LombokBuilder.class)
public class FreeSpaceResponse {

    long freeSpace;

    @JsonPOJOBuilder(withPrefix = "")
    public static class LombokBuilder {
    }
}
