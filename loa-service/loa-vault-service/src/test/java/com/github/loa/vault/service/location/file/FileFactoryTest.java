package com.github.loa.vault.service.location.file;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.File;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

class FileFactoryTest {

    private FileFactory fileFactory;

    @BeforeEach
    void setup() {
        fileFactory = new FileFactory();
    }

    @Test
    void testNewFile() {
        final File result = fileFactory.newFile("testpath", "testname.zip");

        assertThat(result.getPath(), is("testpath" + File.separator + "testname.zip"));
    }
}