package com.github.loa.downloader.source.configuration;

import com.github.loa.downloader.source.domain.DocumentSourceType;
import org.springframework.context.annotation.PropertySource;

@PropertySource("loa.downloader.source")
public class DocumentSourceConfiguration {

    private DocumentSourceType type;
}
