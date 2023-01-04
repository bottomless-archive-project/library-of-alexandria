package com.github.bottomlessarchive.loa.file;

import org.junit.jupiter.api.Test;

import java.io.File;
import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;

class FileManipulatorServiceTest {

    private final FileManipulatorService underTest = new FileManipulatorService();

    @Test
    void testNewFile() {
        final Path result = underTest.newFile("testpath", "testname.zip");

        assertThat(result.toString())
                .isEqualTo("testpath" + File.separator + "testname.zip");
    }
}
