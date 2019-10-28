package com.github.loa.source.folder.configuration;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties("loa.source.folder")
public class FolderDocumentSourceConfiguration {

    private String location;
}
