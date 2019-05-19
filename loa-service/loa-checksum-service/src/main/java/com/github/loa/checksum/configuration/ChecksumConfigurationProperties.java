package com.github.loa.checksum.configuration;

import com.github.loa.checksum.domain.ChecksumType;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * Contains the configuration properties for the checksum.
 */
@Data
@Component
@ConfigurationProperties("loa.checksum")
public class ChecksumConfigurationProperties {

    /**
     * The type of the checksum.
     */
    private ChecksumType type;
}
