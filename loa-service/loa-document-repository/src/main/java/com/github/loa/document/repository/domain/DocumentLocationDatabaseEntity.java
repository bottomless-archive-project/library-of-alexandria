package com.github.loa.document.repository.domain;

import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Getter
@Setter
@Document("documentLocation")
public class DocumentLocationDatabaseEntity {

    @Id
    private String id;
    private String url;
    private String source;
    private int downloaderVersion;
}
