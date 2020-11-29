package com.github.loa.administrator.command.compressor;

import com.github.loa.compression.domain.DocumentCompression;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.ConstructorBinding;

@Getter
@ConstructorBinding
@RequiredArgsConstructor
@ConfigurationProperties("loa.command.silent-compressor")
public class SilentCompressorConfigurationProperties {

    private final DocumentCompression algorithm;

    public boolean hasAlgorithm() {
        return algorithm != null;
    }
}
