package com.github.loa.indexer.service.search.request.mapping;

import com.github.loa.indexer.service.search.request.mapping.domain.MappingLoadingException;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Files;

@Service
@RequiredArgsConstructor
public class MappingConfigurationFactory {

    private final ResourceLoader resourceLoader;

    public String newDocumentMappingConfiguration() {
        try {
            return new String(Files.readAllBytes(
                    resourceLoader.getResource("classpath:indexer/mapping.json").getFile().toPath()));
        } catch (IOException e) {
            throw new MappingLoadingException("Unable to load the mapping for the index!", e);
        }
    }
}
