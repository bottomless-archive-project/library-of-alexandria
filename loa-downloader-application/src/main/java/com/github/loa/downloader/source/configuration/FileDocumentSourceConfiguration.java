package com.github.loa.downloader.source.configuration;

import com.github.loa.downloader.source.domain.file.FileEncodingType;
import lombok.Data;
import org.springframework.context.annotation.PropertySource;

@Data
@PropertySource("loa.downloader.source.file")
public class FileDocumentSourceConfiguration {

    private String location;
    private FileEncodingType encoding;
}
