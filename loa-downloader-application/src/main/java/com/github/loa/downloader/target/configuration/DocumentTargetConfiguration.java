package com.github.loa.downloader.target.configuration;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties("loa.downloader.target")
public class DocumentTargetConfiguration {

    private String temporaryLocation;
    private String location;
}
