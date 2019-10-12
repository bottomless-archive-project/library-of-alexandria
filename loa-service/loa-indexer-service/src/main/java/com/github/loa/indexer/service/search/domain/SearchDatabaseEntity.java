package com.github.loa.indexer.service.search.domain;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class SearchDatabaseEntity {

    private SearchAttachmentEntity attachment;

    @JsonProperty("page_count")
    private int pageCount;
}
