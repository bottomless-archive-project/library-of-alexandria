package com.github.loa.vault.view.request.domain;

import com.github.loa.compression.domain.DocumentCompression;
import lombok.Data;

@Data
public class RecompressRequest {

    private DocumentCompression compression;
}
