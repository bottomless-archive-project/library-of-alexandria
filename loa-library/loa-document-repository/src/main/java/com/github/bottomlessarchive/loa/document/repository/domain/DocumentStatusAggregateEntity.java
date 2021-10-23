package com.github.bottomlessarchive.loa.document.repository.domain;

import lombok.Getter;
import lombok.Setter;
import org.bson.codecs.pojo.annotations.BsonId;

@Getter
@Setter
public class DocumentStatusAggregateEntity {

    @BsonId
    private String id;
    private int count;
}
