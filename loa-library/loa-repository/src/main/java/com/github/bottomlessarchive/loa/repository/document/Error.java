package com.github.bottomlessarchive.loa.repository.document;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum Error {

    DUPLICATE(11000);

    private final int errorCode;

    public boolean hasErrorCode(final int expectedErrorCode) {
        return errorCode == expectedErrorCode;
    }
}
