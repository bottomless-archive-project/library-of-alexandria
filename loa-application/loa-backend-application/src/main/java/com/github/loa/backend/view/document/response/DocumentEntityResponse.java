package com.github.loa.backend.view.document.response;

import com.github.loa.document.service.domain.DocumentStatus;
import com.github.loa.document.service.domain.DocumentType;
import lombok.Builder;
import lombok.Getter;

import java.net.URL;
import java.time.Instant;

@Getter
@Builder
public class DocumentEntityResponse {

    private final String id;
    private final DocumentType type;
    private final DocumentStatus status;
    private final Instant downloadDate;
}
