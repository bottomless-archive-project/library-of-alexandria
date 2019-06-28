package com.github.loa.downloader.command.batch.generator;

import com.github.loa.source.configuration.file.FileDocumentSourceConfiguration;
import com.github.loa.source.service.file.FileSourceFactory;
import com.morethanheroic.taskforce.generator.Generator;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Optional;

@Service
@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
@RequiredArgsConstructor
@ConditionalOnProperty(name = "loa.source.type", havingValue = "file")
public class FileDocumentLocationRecordReader implements Generator<String> {

    private final FileDocumentSourceConfiguration fileDocumentSourceConfiguration;
    private final FileSourceFactory fileSourceFactory;

    private BufferedReader reader;

    @Override
    public Optional<String> generate() {
        try {
            return Optional.ofNullable(reader.readLine());
        } catch (IOException e) {
            return Optional.empty();
        }
    }

    @Override
    public void open() {
        reader = new BufferedReader(new InputStreamReader(fileSourceFactory.newInputStream(
                fileDocumentSourceConfiguration.getLocation())));
    }

    @Override
    public void close() {
        try {
            reader.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
