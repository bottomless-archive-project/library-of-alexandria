package com.github.loa.vault.view.request;

import com.github.loa.document.service.domain.DocumentType;
import lombok.Data;

@Data
public class ArchiveDocumentRequest {

    private DocumentType type;
    private String location;
    private String source;
}
