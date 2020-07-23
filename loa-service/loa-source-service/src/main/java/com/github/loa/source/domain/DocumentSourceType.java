package com.github.loa.source.domain;

/**
 * Describe the various type of sources where document locations could be gathered.
 */
public enum DocumentSourceType {

    /**
     * The source data are loaded from a file.
     */
    FILE,

    /**
     * The source data are parsed from the Common Crawl corpus.
     *
     * @see <a href="http://commoncrawl.org/">Common Crawl</a>
     */
    COMMON_CRAWL
}
