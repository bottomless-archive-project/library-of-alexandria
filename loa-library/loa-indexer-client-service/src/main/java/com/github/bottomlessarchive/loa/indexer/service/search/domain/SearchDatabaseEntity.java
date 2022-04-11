package com.github.bottomlessarchive.loa.indexer.service.search.domain;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.github.bottomlessarchive.loa.document.service.domain.DocumentType;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class SearchDatabaseEntity {

    private String title;
    private String language;
    private String author;
    private String date;
    private DocumentType type;

    @JsonProperty("page_count")
    private int pageCount;
}
