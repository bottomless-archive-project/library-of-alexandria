package com.github.bottomlessarchive.loa.document.service.location;

import com.github.bottomlessarchive.loa.location.domain.link.StringLink;
import com.github.bottomlessarchive.loa.location.service.validation.DocumentLocationValidator;
import com.github.bottomlessarchive.loa.location.domain.DocumentLocation;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class DocumentLocationValidatorTest {

    private static final String EMPTY_LINK = "";

    private final DocumentLocationValidator underTest = new DocumentLocationValidator();

    @ParameterizedTest
    @CsvSource(
            value = {
                    "http://www.example.com/test.pdf",
                    "http://www.example.com/test.doc",
                    "http://www.example.com/test.docx",
                    "http://www.example.com/test.ppt",
                    "http://www.example.com/test.pptx",
                    "http://www.example.com/test.xls",
                    "http://www.example.com/test.xlsx",
                    "http://www.example.com/test.rtf",
                    "http://www.example.com/test.mobi",
                    "http://www.example.com/test.epub",
                    "http://www.example.com/test.epub?queryparam=value"
            }
    )
    void testValidDocumentLocationWithValidDocuments(final String documentLocation) {
        final boolean result = underTest.validDocumentLocation(
                DocumentLocation.builder()
                        .location(StringLink.builder()
                                .link(documentLocation)
                                .build()
                        )
                        .build()
        );

        assertTrue(result);
    }

    @ParameterizedTest
    @CsvSource(
            value = {
                    "http://www.example.com/test.exe",
                    "http://www.example.com/test.md",
                    "http://www.example.com/test"
            }
    )
    void testValidDocumentLocationWithInvalidDocuments(final String documentLocation) {
        final boolean result = underTest.validDocumentLocation(
                DocumentLocation.builder()
                        .location(StringLink.builder()
                                .link(documentLocation)
                                .build()
                        )
                        .build()
        );

        assertFalse(result);
    }


    @ParameterizedTest
    @CsvSource(
            value = {
                    "helloWorld",
                    "http://askdoctorkcom-back-strengthening-exercises-37d5.pdf/"
            }
    )
    void testInvalidDocumentLocation(final String documentLocation) {
        final boolean result = underTest.validDocumentLocation(
                DocumentLocation.builder()
                        .location(StringLink.builder().link(documentLocation).build())
                        .build()
        );

        assertFalse(result);
    }

    // Empty string makes JUnit throw an invalid CSV error, so we need to test it in its own method
    @Test
    void testEmptyDocumentLocation() {
        final boolean result = underTest.validDocumentLocation(
                DocumentLocation.builder()
                        .location(StringLink.builder()
                                .link(EMPTY_LINK)
                                .build()
                        )
                        .build()
        );

        assertFalse(result);
    }
}
