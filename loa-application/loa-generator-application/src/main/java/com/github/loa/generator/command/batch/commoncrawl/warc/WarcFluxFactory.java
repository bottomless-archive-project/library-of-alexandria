package com.github.loa.generator.command.batch.commoncrawl.warc;

import com.morethanheroic.warc.service.WarcRecordStreamFactory;
import com.morethanheroic.warc.service.record.domain.WarcRecord;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.stream.Stream;

@Service
public class WarcFluxFactory {

    public Flux<WarcRecord> buildWarcRecordFlux(final File warcLocation) {
        return Flux.fromStream(() -> buildWarcRecordStream(warcLocation));
    }

    private Stream<WarcRecord> buildWarcRecordStream(final File warcLocation) {
        try {
            return WarcRecordStreamFactory.streamOf(new FileInputStream(warcLocation))
                    .filter(WarcRecord::isResponse);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
