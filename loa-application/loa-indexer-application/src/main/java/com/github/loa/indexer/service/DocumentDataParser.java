package com.github.loa.indexer.service;

import com.github.loa.document.service.domain.DocumentEntity;
import com.github.loa.indexer.command.DocumentParser;
import com.github.loa.indexer.service.index.domain.DocumentMetadata;
import com.github.loa.vault.client.service.VaultClientService;
import com.github.pemistahl.lingua.api.Language;
import com.github.pemistahl.lingua.api.LanguageDetector;
import com.github.pemistahl.lingua.api.LanguageDetectorBuilder;
import lombok.RequiredArgsConstructor;
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

@Service
@RequiredArgsConstructor
public class DocumentDataParser {

    private final DocumentParser documentParser;
    private final VaultClientService vaultClientService;
    //TODO: This should come from a config!
    private final LanguageDetector languageDetector = LanguageDetectorBuilder.fromAllBuiltInLanguages().build();

    public Mono<DocumentMetadata> parseDocumentData(final DocumentEntity documentEntity) {
        return vaultClientService.queryDocument(documentEntity)
                .map(documentContent -> parseDocumentMetadata(documentEntity, documentContent));
    }

    private DocumentMetadata parseDocumentMetadata(final DocumentEntity documentEntity, final byte[] documentContents) {
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
        if (documentEntity.isPdf()) {
            try (final PDDocument pdDocument = documentParser.parseDocument(documentContents)) {
                pageCount = pdDocument.getNumberOfPages();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }

        final Language language = languageDetector.detectLanguageOf(handler.toString());

        return DocumentMetadata.builder()
                .id(documentEntity.getId())
                .title(metadata.get(TikaCoreProperties.TITLE))
                .author(metadata.get(TikaCoreProperties.CREATOR))
                .date(metadata.get(TikaCoreProperties.CREATED))
                .content(handler.toString())
                .language(language.getIsoCode())
                .pageCount(pageCount)
                .build();
    }
}
