package com.github.loa.compression.configuration;

import com.github.loa.compression.domain.DocumentCompression;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties("loa.compression")
public class DocumentCompressionConfigurationProperties {

    private DocumentCompression algorithm;
}