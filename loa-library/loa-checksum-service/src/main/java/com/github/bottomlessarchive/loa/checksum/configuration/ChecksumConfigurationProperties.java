package com.github.bottomlessarchive.loa.checksum.configuration;

import com.github.bottomlessarchive.loa.checksum.domain.ChecksumType;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.ConstructorBinding;

/**
 * Contains the configuration properties for the checksum.
 */
@Getter
@ConstructorBinding
@RequiredArgsConstructor
@ConfigurationProperties("loa.checksum")
public class ChecksumConfigurationProperties {

    /**
     * The type of the checksum.
     */
    private final ChecksumType type;
}
