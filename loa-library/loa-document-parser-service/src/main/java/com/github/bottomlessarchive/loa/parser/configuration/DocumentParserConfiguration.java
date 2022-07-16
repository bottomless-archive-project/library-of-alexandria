package com.github.bottomlessarchive.loa.parser.configuration;

import com.github.bottomlessarchive.loa.document.service.domain.DocumentType;
import com.github.bottomlessarchive.loa.parser.service.parser.ExceptionParser;
import com.github.pemistahl.lingua.api.LanguageDetector;
import com.github.pemistahl.lingua.api.LanguageDetectorBuilder;
import org.apache.tika.mime.MediaType;
import org.apache.tika.mime.MediaTypeRegistry;
import org.apache.tika.parser.CompositeParser;
import org.apache.tika.parser.Parser;
import org.apache.tika.parser.ParserDecorator;
import org.apache.tika.parser.epub.EpubParser;
import org.apache.tika.parser.microsoft.OfficeParser;
import org.apache.tika.parser.microsoft.ooxml.OOXMLParser;
import org.apache.tika.parser.microsoft.rtf.RTFParser;
import org.apache.tika.parser.pdf.PDFParser;
import org.apache.tika.parser.xml.FictionBookParser;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Set;

@Configuration
public class DocumentParserConfiguration {

    @Bean
    public Parser documentParser() {
        final CompositeParser compositeParser = new CompositeParser(new MediaTypeRegistry(),
                ParserDecorator.withTypes(new PDFParser(), Set.of(
                        MediaType.parse(DocumentType.PDF.getMimeType())
                )),
                ParserDecorator.withTypes(new RTFParser(), Set.of(
                        MediaType.parse(DocumentType.RTF.getMimeType())
                )),
                ParserDecorator.withTypes(new EpubParser(), Set.of(
                        MediaType.parse(DocumentType.EPUB.getMimeType())
                )),
                ParserDecorator.withTypes(new OfficeParser(), Set.of(
                        MediaType.parse(DocumentType.DOC.getMimeType()),
                        MediaType.parse(DocumentType.PPT.getMimeType()),
                        MediaType.parse(DocumentType.XLS.getMimeType())
                )),
                ParserDecorator.withTypes(new OOXMLParser(), Set.of(
                        MediaType.parse(DocumentType.DOCX.getMimeType()),
                        MediaType.parse(DocumentType.PPTX.getMimeType()),
                        MediaType.parse(DocumentType.XLSX.getMimeType())
                )),
                ParserDecorator.withTypes(new FictionBookParser(), Set.of(
                        MediaType.parse(DocumentType.FB2.getMimeType())
                ))
        );

        compositeParser.setFallback(new ExceptionParser());

        return compositeParser;
    }

    @Bean
    public LanguageDetector languageDetector() {
        return LanguageDetectorBuilder.fromAllLanguages()
                .withPreloadedLanguageModels()
                .build();
    }
}
