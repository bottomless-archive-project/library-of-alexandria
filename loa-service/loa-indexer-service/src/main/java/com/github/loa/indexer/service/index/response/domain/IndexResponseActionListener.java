package com.github.loa.indexer.service.index.response.domain;

import com.github.loa.document.service.DocumentManipulator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.elasticsearch.action.ActionListener;
import org.elasticsearch.action.index.IndexResponse;

@Slf4j
@RequiredArgsConstructor
public class IndexResponseActionListener implements ActionListener<IndexResponse> {

    private final DocumentManipulator documentManipulator;

    @Override
    public void onResponse(final IndexResponse indexResponse) {
        documentManipulator.markIndexed(indexResponse.getId());
    }

    @Override
    public void onFailure(final Exception e) {
        log.error("Failed to index document!", e);
    }
}
