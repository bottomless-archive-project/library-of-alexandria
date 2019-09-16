package com.github.loa.document.repository.domain;

import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.index.CompoundIndexes;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;

@Getter
@Setter
@Document("document")
@CompoundIndexes({
        @CompoundIndex(name = "unsaved_query", def = "{'status' : 1}"),
        @CompoundIndex(name = "unique_file", def = "{'checksum' : 1, 'fileSize': 1, 'type': 1}")
})
public class DocumentDatabaseEntity {

    @Id
    private String id;
    private String type;
    private String status;
    private String compression;
    private String checksum;
    private long fileSize;
    private Instant downloadDate;
    private int downloaderVersion;
}
