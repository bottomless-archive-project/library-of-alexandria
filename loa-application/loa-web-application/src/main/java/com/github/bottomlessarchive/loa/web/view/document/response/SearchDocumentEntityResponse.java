package com.github.bottomlessarchive.loa.web.view.document.response;

import com.github.bottomlessarchive.loa.type.domain.DocumentType;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public class SearchDocumentEntityResponse {

    private final String id;
    private final String title;
    private final String author;
    private final List<String> description;
    private final String language;
    private final int pageCount;
    private final DocumentType type;
}
