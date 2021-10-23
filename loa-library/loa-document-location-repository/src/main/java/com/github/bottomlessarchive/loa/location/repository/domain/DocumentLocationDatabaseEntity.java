package com.github.bottomlessarchive.loa.location.repository.domain;

import lombok.Getter;
import lombok.Setter;
import org.bson.codecs.pojo.annotations.BsonId;

@Getter
@Setter
public class DocumentLocationDatabaseEntity {

    @BsonId
    private byte[] id;
    private String url;
    private String source;
    private int downloaderVersion;
}
