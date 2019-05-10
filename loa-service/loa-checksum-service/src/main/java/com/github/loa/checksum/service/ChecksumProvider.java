package com.github.loa.checksum.service;

/**
 * This interface is responsible for providing checksum values for files in the stage location.
 */
public interface ChecksumProvider {

    String checksum(final String documentId);
}
