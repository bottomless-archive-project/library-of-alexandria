package com.github.bottomlessarchive.loa.stage.service.domain.exception;

import lombok.experimental.StandardException;

/**
 * Thrown when an error happens while accessing content on the staging area.
 */
@StandardException
public class StageAccessException extends RuntimeException {
}
