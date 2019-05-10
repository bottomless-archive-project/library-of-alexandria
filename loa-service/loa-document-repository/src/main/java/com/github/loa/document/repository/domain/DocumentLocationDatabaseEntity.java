package com.github.loa.document.repository.domain;

import dev.morphia.annotations.Entity;
import dev.morphia.annotations.Id;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity(value = "documentLocation", noClassnameStored = true)
public class DocumentLocationDatabaseEntity {

    @Id
    private String id;
    private String url;
    private String source;
    private int downloaderVersion;
}
