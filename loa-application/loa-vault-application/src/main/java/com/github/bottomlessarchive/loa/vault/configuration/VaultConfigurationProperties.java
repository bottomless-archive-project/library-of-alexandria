package com.github.bottomlessarchive.loa.vault.configuration;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

import java.nio.file.Path;

/**
 * Holds the configuration properties to the vault.
 *
 * @param name                The unique name of the vault. This name will be saved when archiving new documents and will be used to
 *                            identify the vault that holds a given document. Do not change this after creating the vault because the
 *                            documents previously archived by this vault are not going to be accessible.
 * @param modificationEnabled If this vault should be able to modify documents (e.g. remove) after they are archived. If your vault is
 *                            available publicly on the internet then set this to false!
 * @param archiving           If this vault should archive new documents.
 * @param versionNumber       This version number will be saved to the database for every document that has been archived. It will be used
 *                            later on if it's necessary to run cleanup or fixing tasks that are specific to a given version.
 * @param parallelism         The number of documents that should be saved in parallel at the same time.
 */
@Validated
@ConfigurationProperties("loa.vault")
public record VaultConfigurationProperties(

        @NotNull
        String name,

        boolean modificationEnabled,
        boolean archiving,

        @Min(1)
        int versionNumber,

        @Min(1)
        int parallelism,

        @NotNull
        Path stagingDirectory
) {
}
