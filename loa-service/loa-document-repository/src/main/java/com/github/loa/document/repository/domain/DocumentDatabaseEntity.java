package com.github.loa.document.repository.domain;

import dev.morphia.annotations.*;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;

@Getter
@Setter
@Entity("document")
@Indexes(
        @Index(fields = {@Field("checksum"), @Field("fileSize")})
)
public class DocumentDatabaseEntity {

    @Id
    private String id;
    private String url;
    private String checksum;
    private long fileSize;
    private String source;
    private Instant downloadDate;
    private int downloaderVersion;
    private String status;
    private String compression;
}
