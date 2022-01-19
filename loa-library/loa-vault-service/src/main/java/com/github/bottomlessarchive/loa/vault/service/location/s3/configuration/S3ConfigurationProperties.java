package com.github.bottomlessarchive.loa.vault.service.location.s3.configuration;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.net.URI;

@ConfigurationProperties("loa.vault.location.s3")
public record S3ConfigurationProperties(

        String host,
        String port,
        String accessKey,
        String secretKey,
        String region,
        String bucketName
) {

    public URI getEndpointLocation() {
        return URI.create("http://" + host + ":" + port + "/");
    }
}
