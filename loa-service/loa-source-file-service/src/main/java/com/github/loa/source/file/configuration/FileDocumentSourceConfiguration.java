package com.github.loa.source.file.configuration;

import com.github.loa.source.file.service.domain.FileEncodingType;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties("loa.source.file")
public class FileDocumentSourceConfiguration {

    private String location;
    private FileEncodingType encoding;
}
