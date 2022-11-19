package com.github.bottomlessarchive.loa.parser.service;

import com.github.bottomlessarchive.loa.parser.domain.ParsingResult;
import com.github.bottomlessarchive.loa.parser.domain.ParsingException;
import com.github.bottomlessarchive.loa.type.domain.DocumentType;
import com.github.pemistahl.lingua.api.Language;
import com.github.pemistahl.lingua.api.LanguageDetector;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.tika.exception.TikaException;
import org.apache.tika.io.TikaInputStream;
import org.apache.tika.metadata.Metadata;
import org.apache.tika.metadata.PagedText;
import org.apache.tika.metadata.TikaCoreProperties;
import org.apache.tika.parser.ParseContext;
import org.apache.tika.parser.Parser;
import org.apache.tika.sax.BodyContentHandler;
import org.springframework.stereotype.Service;
import org.xml.sax.ContentHandler;
import org.xml.sax.SAXException;

import java.io.IOException;
import java.io.InputStream;
import java.util.Optional;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class DocumentDataParser {

    private final Parser documentParser;
    private final LanguageDetector languageDetector;

    public ParsingResult parseDocumentMetadata(final UUID documentId, final DocumentType documentType,
            final InputStream documentContents) {
        if (!isParsableType(documentType)) {
            return ParsingResult.builder()
                    .id(documentId)
                    .type(documentType)
                    .build();
        }

        final ContentHandler contentHandler = buildContentHandler();
        final Metadata metadata = buildMetadata(documentType);
        final ParseContext context = buildParseContext();

        try (InputStream inputStream = TikaInputStream.get(documentContents)) {
            documentParser.parse(inputStream, contentHandler, metadata, context);

            final int pageCount = parsePageCount(metadata);
            final String language = detectLanguage(contentHandler);

            return ParsingResult.builder()
                    .id(documentId)
                    .title(metadata.get(TikaCoreProperties.TITLE))
                    .author(metadata.get(TikaCoreProperties.CREATOR))
                    .date(metadata.get(TikaCoreProperties.CREATED))
                    .content(contentHandler.toString())
                    .language(language)
                    .type(documentType)
                    .pageCount(pageCount)
                    .build();
        } catch (final IOException | SAXException | TikaException e) {
            throw new ParsingException("Unable to parse document with id: " + documentId + "!", e);
        }
    }

    private boolean isParsableType(final DocumentType documentType) {
        return documentType != DocumentType.MOBI;
    }

    private ContentHandler buildContentHandler() {
        return new BodyContentHandler(-1);
    }

    private Metadata buildMetadata(final DocumentType documentType) {
        final Metadata metadata = new Metadata();

        metadata.add("Content-Type", documentType.getMimeType());

        return metadata;
    }

    private ParseContext buildParseContext() {
        return new ParseContext();
    }

    private int parsePageCount(final Metadata metadata) {
        return Optional.ofNullable(metadata.getInt(PagedText.N_PAGES))
                .orElse(0);
    }

    private String detectLanguage(final ContentHandler contentHandler) {
        final Language language = languageDetector.detectLanguageOf(contentHandler.toString());

        return language.getIsoCode639_1().toString();
    }
}
