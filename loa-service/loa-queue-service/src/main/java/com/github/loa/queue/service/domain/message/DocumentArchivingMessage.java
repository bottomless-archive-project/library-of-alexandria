package com.github.loa.queue.service.domain.message;

import lombok.Builder;
import lombok.Getter;

import java.io.InputStream;
import java.io.OutputStream;

@Getter
@Builder
public class DocumentArchivingMessage {

    private String type;
    private String location;
    private String source;
}
