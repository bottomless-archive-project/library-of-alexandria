package com.github.loa.vault.service.domain;

import com.github.loa.document.service.domain.DocumentType;
import lombok.Builder;
import lombok.Getter;
import org.springframework.core.io.Resource;

@Getter
@Builder
public class DocumentArchivingContext {

    private DocumentType type;
    private String location;
    private String source;
    private Resource contents;
}
