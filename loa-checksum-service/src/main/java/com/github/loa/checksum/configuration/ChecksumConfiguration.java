package com.github.loa.checksum.configuration;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties("loa.checksum")
public class ChecksumConfiguration {

    private String type;
}
