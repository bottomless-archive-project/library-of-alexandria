package com.github.loa.source.domain;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;
import lombok.Builder;
import lombok.Getter;

import java.net.URL;

//TODO: This library shouldn't know about jackson at all! Find a way to remove the jackson annotations.
@Getter
@Builder(builderClassName = "DocumentSourceItemBuilder")
@JsonDeserialize(builder = DocumentSourceItem.DocumentSourceItemBuilder.class)
public class DocumentSourceItem {

    private final String sourceName;
    private final URL documentLocation;

    @JsonPOJOBuilder(withPrefix = "")
    public static class DocumentSourceItemBuilder {
        // Lombok will add constructor, setters, build method
    }
}
