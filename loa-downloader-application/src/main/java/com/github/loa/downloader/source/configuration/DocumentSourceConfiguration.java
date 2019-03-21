package com.github.loa.downloader.source.configuration;

import com.github.loa.downloader.source.domain.DocumentSourceType;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties("loa.downloader.source")
public class DocumentSourceConfiguration {

    private String name;
    private DocumentSourceType type;
}
