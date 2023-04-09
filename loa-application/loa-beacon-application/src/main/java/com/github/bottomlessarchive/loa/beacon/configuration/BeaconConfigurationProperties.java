package com.github.bottomlessarchive.loa.beacon.configuration;

import jakarta.validation.constraints.NotNull;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

import java.nio.file.Path;

@Validated
@ConfigurationProperties("loa.beacon")
public record BeaconConfigurationProperties(

        @NotNull
        Path storageDirectory,

        @NotNull
        Path stagingDirectory
) {
}
