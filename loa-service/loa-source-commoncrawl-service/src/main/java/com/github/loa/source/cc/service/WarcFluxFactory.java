package com.github.loa.source.cc.service;

import com.morethanheroic.warc.service.WarcRecordStreamFactory;
import com.morethanheroic.warc.service.record.domain.WarcRecord;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;

import java.net.URL;

@Service
public class WarcFluxFactory {

    public Flux<WarcRecord> buildWarcRecordFlux(final URL warcLocation) {
        return Flux.fromStream(() -> WarcRecordStreamFactory.streamOf(warcLocation))
                .filter(WarcRecord::isResponse);
    }
}
