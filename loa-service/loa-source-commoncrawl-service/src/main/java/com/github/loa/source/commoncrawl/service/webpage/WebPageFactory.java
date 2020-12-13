package com.github.loa.source.commoncrawl.service.webpage;

import com.github.bottomlessarchive.warc.service.content.response.domain.ResponseContentBlock;
import com.github.bottomlessarchive.warc.service.record.domain.WarcRecord;
import com.github.loa.source.commoncrawl.service.webpage.domain.WebPage;
import org.springframework.stereotype.Service;

@Service
public class WebPageFactory {

    /**
     * Parse the content of a {@link WarcRecord} into a {@link WebPage}.
     *
     * @param warcRecord the WARC record to convert
     * @return the result of the conversion
     */
    public WebPage newWebPage(final WarcRecord<ResponseContentBlock> warcRecord) {
        final String warcRecordUrl = warcRecord.getHeader("WARC-Target-URI");
        final String contentString = warcRecord.getContentBlock().getPayloadAsString();

        return WebPage.builder()
                .url(warcRecordUrl)
                .content(contentString)
                .build();
    }
}
