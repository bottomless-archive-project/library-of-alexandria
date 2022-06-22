package com.github.bottomlessarchive.loa.stage.view.document.controller;

import com.github.bottomlessarchive.loa.stage.configuration.StagingConfigurationProperties;
import com.github.bottomlessarchive.loa.stage.util.AutoDeleteFileSystemResource;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.springframework.core.io.Resource;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.nio.file.Path;

@RestController
@RequiredArgsConstructor
public class StageDocumentController {

    private final StagingConfigurationProperties stagingConfigurationProperties;

    @SneakyThrows
    @PostMapping("/document/{documentId}")
    public void persistDocument(@PathVariable final String documentId, @RequestParam("file") final MultipartFile file) {
        file.transferTo(Path.of(stagingConfigurationProperties.path()).resolve(documentId));
    }

    @GetMapping("/document/{documentId}")
    public Resource serveDocument(@PathVariable final String documentId) {
        return new AutoDeleteFileSystemResource(Path.of(stagingConfigurationProperties.path()).resolve(documentId));
    }
}
