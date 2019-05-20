package com.github.loa.document.repository.domain;

import dev.morphia.annotations.*;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;

@Getter
@Setter
@Entity(value = "document", noClassnameStored = true)
@Indexes(
        @Index(fields = {@Field("checksum"), @Field("fileSize"), @Field("type")})
)
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
