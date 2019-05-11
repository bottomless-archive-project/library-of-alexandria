package com.github.loa.web.view.document.response;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class SearchDocumentEntityResponse {

    private final String title;
    private final String author;
    private final String description;
    private final String language;
    private final DocumentEntityResponse document;
}
