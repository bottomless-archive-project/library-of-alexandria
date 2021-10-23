package com.github.bottomlessarchive.loa.compression.configuration;

import com.github.bottomlessarchive.loa.compression.domain.DocumentCompression;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.ConstructorBinding;

/**
 * Contains the configuration properties for the compression.
 */
@Getter
@ConstructorBinding
@RequiredArgsConstructor
@ConfigurationProperties("loa.compression")
public class CompressionConfigurationProperties {

    private final DocumentCompression algorithm;
}
