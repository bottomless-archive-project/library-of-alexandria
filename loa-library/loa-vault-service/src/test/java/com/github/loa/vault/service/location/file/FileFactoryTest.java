package com.github.loa.vault.service.location.file;

import org.junit.jupiter.api.Test;

import java.io.File;
import java.nio.file.Path;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

class FileFactoryTest {

    private final FileFactory underTest = new FileFactory();

    @Test
    void testNewFile() {
        final Path result = underTest.newFile("testpath", "testname.zip");

        assertThat(result.toString(), is("testpath" + File.separator + "testname.zip"));
    }
}
