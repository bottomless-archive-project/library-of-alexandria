package com.github.bottomlessarchive.loa.location.service.validation.extension;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class DelegatingFileExtensionValidatorTest {

    private static final String EMPTY_LINK = "";

    private final DelegatingFileExtensionValidator underTest = new DelegatingFileExtensionValidator();


    @ParameterizedTest
    @CsvSource(
            value = {
                    "/test.pdf",
                    "/test.doc",
                    "/test.docx",
                    "/test.ppt",
                    "/test.pptx",
                    "/test.xls",
                    "/test.xlsx",
                    "/test.rtf",
                    "/test.mobi",
                    "/test.epub"
            }
    )
    void testValidDocumentLocationWithValidDocuments(final String documentLocation) {
        final boolean result = underTest.isValidPathWithExtension(documentLocation);

        assertTrue(result);
    }

    @ParameterizedTest
    @CsvSource(
            value = {
                    "/test.exe",
                    "/test.md",
                    "/test"
            }
    )
    void testValidDocumentLocationWithInvalidDocuments(final String documentLocation) {
        final boolean result = underTest.isValidPathWithExtension(documentLocation);

        assertFalse(result);
    }

    // Empty string makes JUnit throw an invalid CSV error, so we need to test it in its own method
    @Test
    void testEmptyDocumentLocation() {
        final boolean result = underTest.isValidPathWithExtension(EMPTY_LINK);

        assertFalse(result);
    }
}
