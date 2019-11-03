package com.github.loa.generator.command.batch.commoncrawl.warc;

import com.github.loa.downloader.download.service.file.FileDownloader;
import com.github.loa.downloader.service.url.URLConverter;
import com.github.loa.stage.configuration.StageConfigurationProperties;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.io.File;

@Slf4j
@Service
@RequiredArgsConstructor
public class WarcDownloader {

    private final URLConverter urlConverter;
    private final FileDownloader fileDownloader;
    private final StageConfigurationProperties stageConfigurationProperties;

    public Mono<File> downloadWarcFile(final String warcLocation) {
        return urlConverter.convertOrThrow("https://commoncrawl.s3.amazonaws.com/" + warcLocation)
                .doOnNext(warcUrl -> log.info("Started to download warc file at location: {}!", warcUrl))
                .flatMap(warcUrl -> fileDownloader.downloadFile(warcUrl,
                        new File(stageConfigurationProperties.getLocation(), "under-progress.warc")));
    }
}
