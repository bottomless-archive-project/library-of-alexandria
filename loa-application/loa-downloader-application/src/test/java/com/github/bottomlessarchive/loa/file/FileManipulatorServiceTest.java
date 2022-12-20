package com.github.bottomlessarchive.loa.file;

import org.junit.jupiter.api.Test;

import java.io.File;
import java.nio.file.Path;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

class FileManipulatorServiceTest {

    private final FileManipulatorService underTest = new FileManipulatorService();

    @Test
    void testNewFile() {
        final Path result = underTest.newFile("testpath", "testname.zip");

        assertThat(result.toString(), is("testpath" + File.separator + "testname.zip"));
    }
}
