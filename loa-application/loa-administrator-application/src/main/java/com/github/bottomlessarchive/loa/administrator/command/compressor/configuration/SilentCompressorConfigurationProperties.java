package com.github.bottomlessarchive.loa.administrator.command.compressor.configuration;

import com.github.bottomlessarchive.loa.compression.domain.DocumentCompression;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties("loa.command.silent-compressor")
public record SilentCompressorConfigurationProperties(

        DocumentCompression algorithm
) {

    public boolean hasAlgorithm() {
        return algorithm != null;
    }
}
