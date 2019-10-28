package com.github.loa.source.domain;

public enum DocumentSourceType {

    /**
     * The source data (URLs) are loaded from a file.
     */
    FILE,

    /**
     * The source data are parsed from the Common Crawl corpus.
     *
     * @see <a href="http://commoncrawl.org/">Common Crawl</a>
     */
    COMMON_CRAWL
}
