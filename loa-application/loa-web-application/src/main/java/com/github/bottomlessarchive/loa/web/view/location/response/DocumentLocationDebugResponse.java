package com.github.bottomlessarchive.loa.web.view.location.response;

import com.github.bottomlessarchive.loa.location.domain.DocumentLocationResultType;
import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class DocumentLocationDebugResponse {

    String id;
    String url;
    String source;
    int downloaderVersion;
    DocumentLocationResultType downloadResultCode;
}
