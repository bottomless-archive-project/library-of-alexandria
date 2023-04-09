package com.github.bottomlessarchive.loa.downloader.configuration;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

import java.nio.file.Path;

@Validated
@ConfigurationProperties("loa.downloader")
public record DownloaderConfigurationProperties(

        /*
         * 1 = M1 - RC-2 -> Updated because normal files had a dot at the end.
         * 2 = RC-3+ -> The update reason is unknown.
         * 3 = 1.0.0-release -> Updated to have a clean slate for the first "real" release.
         * 4 = Skipped
         * 5 = 1.5.1-release -> Updated because the URL encoding logic was changed in the Generator Application.
         */
        @Min(1)
        int versionNumber,

        @NotNull
        SourceLocation source,

        @Min(1)
        int parallelism,

        @NotNull
        Path stagingDirectory
) {
}
