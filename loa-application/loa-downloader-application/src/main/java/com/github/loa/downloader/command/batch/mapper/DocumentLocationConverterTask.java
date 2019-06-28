package com.github.loa.downloader.command.batch.mapper;

import com.morethanheroic.taskforce.task.Task;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Optional;

@Slf4j
@Service
public class DocumentLocationConverterTask implements Task<String, URL> {

    @Override
    public Optional<URL> execute(final String recordUrl) {
        try {
            return Optional.of(new URL(recordUrl));
        } catch (MalformedURLException e) {
            log.warn("Unable to parse url with location: " + recordUrl, e);

            return Optional.empty();
        }
    }
}
