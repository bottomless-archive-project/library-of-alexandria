package com.github.bottomlessarchive.loa.stage.service.location;

import lombok.Builder;

import java.nio.file.Path;

@Builder
public record StageLocation(

        Path location
) {
}
