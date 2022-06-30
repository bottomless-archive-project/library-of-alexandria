package com.github.bottomlessarchive.loa.checksum.domain;

import lombok.experimental.StandardException;

/**
 * This exception is thrown whenever an error happens in the checksum calculation.
 */
@StandardException
public class ChecksumCalculationException extends RuntimeException {
}
