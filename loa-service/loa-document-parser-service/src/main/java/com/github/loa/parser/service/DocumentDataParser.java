package com.github.loa.parser.service;

import com.github.loa.document.service.domain.DocumentEntity;
import com.github.loa.document.service.domain.DocumentType;
import com.github.loa.parser.domain.DocumentMetadata;
import com.github.loa.vault.client.service.VaultClientService;
import com.github.pemistahl.lingua.api.Language;
import com.github.pemistahl.lingua.api.LanguageDetector;
import com.github.pemistahl.lingua.api.LanguageDetectorBuilder;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.tika.exception.TikaException;
import org.apache.tika.metadata.Metadata;
import org.apache.tika.metadata.TikaCoreProperties;
import org.apache.tika.parser.AutoDetectParser;
import org.apache.tika.parser.ParseContext;
import org.apache.tika.parser.Parser;
import org.apache.tika.sax.BodyContentHandler;
import org.springframework.stereotype.Service;
import org.xml.sax.ContentHandler;
import org.xml.sax.SAXException;
import reactor.core.publisher.Mono;

import java.io.ByteArrayInputStream;
import java.io.IOException;

@Slf4j
@Service
@RequiredArgsConstructor
public class DocumentDataParser {

    private final VaultClientService vaultClientService;
    //TODO: This should come from a config!
    private final LanguageDetector languageDetector = LanguageDetectorBuilder.fromAllBuiltInLanguages().build();

    public Mono<DocumentMetadata> parseDocumentData(final DocumentEntity documentEntity) {
        return vaultClientService.queryDocument(documentEntity)
                .map(documentContent -> parseDocumentMetadata(documentEntity.getId(), documentEntity.getType(), documentContent))
                .onErrorContinue((throwable, document) -> log.debug("Failed to parse document!", throwable));
    }

    public DocumentMetadata parseDocumentMetadata(final String documentId, final DocumentType documentType,
            final byte[] documentContents) {
        final Parser parser = new AutoDetectParser();
        final ContentHandler handler = new BodyContentHandler(-1);
        final Metadata metadata = new Metadata();
        final ParseContext context = new ParseContext();

        try {
            parser.parse(new ByteArrayInputStream(documentContents), handler, metadata, context);
        } catch (IOException | SAXException | TikaException e) {
            throw new RuntimeException(e);
        }

        int pageCount = 0;
        if (documentType == DocumentType.PDF) {
            try (final PDDocument pdDocument = PDDocument.load(documentContents)) {
                pageCount = pdDocument.getNumberOfPages();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }

        final Language language = languageDetector.detectLanguageOf(handler.toString());

        return DocumentMetadata.builder()
                .id(documentId)
                .title(metadata.get(TikaCoreProperties.TITLE))
                .author(metadata.get(TikaCoreProperties.CREATOR))
                .date(metadata.get(TikaCoreProperties.CREATED))
                .content(handler.toString())
                .language(language.getIsoCode())
                .type(documentType)
                .pageCount(pageCount)
                .build();
    }
}
