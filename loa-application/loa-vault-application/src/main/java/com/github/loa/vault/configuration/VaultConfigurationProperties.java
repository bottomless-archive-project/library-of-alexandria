package com.github.loa.vault.configuration;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.ConstructorBinding;

/**
 * Holds the configuration properties to the vault.
 */
@Getter
@ConstructorBinding
@RequiredArgsConstructor
@ConfigurationProperties("loa.vault")
public class VaultConfigurationProperties {

    /**
     * The unique name of the vault. This name will be saved when archiving new documents and will be used to identify
     * the vault that holds a given document. Do not change this after creating the vault because the documents
     * previously archived by this vault are not going to be accessible.
     */
    private final String name;

    /**
     * If this vault should be able to modify documents (e.g. remove) after they are archived. If your vault is available
     * publicly on the internet then set this to false!
     */
    private final boolean modificationEnabled;

    /**
     * If this vault should archive new documents.
     */
    private final boolean archiving;

    /**
     * This version number will be saved to the database for every document that has been archived. It will be used
     * later on if it's necessary to run cleanup or fixing tasks that are specific to a given version.
     */
    private final int versionNumber;
}
