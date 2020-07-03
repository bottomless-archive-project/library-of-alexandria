package com.github.loa.vault.service.location.s3.configuration;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties("loa.vault.location.s3")
public class S3ConfigurationProperties {

    private String host;
    private String port;
    private String accessKey;
    private String secretKey;
    private String region;
    private String bucketName;
}
