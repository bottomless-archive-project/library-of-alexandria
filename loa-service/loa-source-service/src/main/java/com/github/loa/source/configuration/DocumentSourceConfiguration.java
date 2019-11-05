package com.github.loa.source.configuration;

import com.github.loa.source.domain.DocumentSourceType;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties("loa.source")
public class DocumentSourceConfiguration {

    private String name;
    private DocumentSourceType type;
}
