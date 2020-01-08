package com.github.loa.vault.service.listener.domain;

import com.github.loa.document.service.domain.DocumentType;
import lombok.Builder;
import lombok.Getter;

import java.io.OutputStream;
import java.net.URL;

@Getter
@Builder
public class ArchivingContext {

    private DocumentType type;
    private URL location;
    private String source;
    private OutputStream content;
}
