package com.github.loa.indexer.service.search.domain;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.github.loa.document.service.domain.DocumentType;
import lombok.Data;

@Data
public class SearchDatabaseEntity {

    private String content;
    private String title;
    private String language;
    private String author;
    private String date;
    private DocumentType type;

    @JsonProperty("page_count")
    private int pageCount;
}
