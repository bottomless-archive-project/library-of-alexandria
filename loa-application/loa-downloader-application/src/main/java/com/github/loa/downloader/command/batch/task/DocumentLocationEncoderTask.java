package com.github.loa.downloader.command.batch.task;

import com.github.loa.url.service.UrlEncoder;
import com.morethanheroic.taskforce.task.Task;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.net.URL;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class DocumentLocationEncoderTask implements Task<URL, URL> {

    private final UrlEncoder urlEncoder;

    @Override
    public Optional<URL> execute(final URL location) {
        return urlEncoder.encode(location);
    }
}
