package com.github.loa.administrator.command.compressor;

import com.github.loa.compression.domain.DocumentCompression;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties("loa.command.silent-compressor")
public class SilentCompressorConfigurationProperties {

    private DocumentCompression algorithm;
    private int parallelismLevel = 10;

    public boolean hasAlgorithm() {
        return algorithm != null;
    }
}
