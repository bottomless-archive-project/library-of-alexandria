package com.github.bottomlessarchive.loa.web.view.user.request;

import lombok.Builder;
import lombok.Getter;
import lombok.extern.jackson.Jacksonized;

@Getter
@Builder
@Jacksonized
public class LoginRequest {

    private final String user;
    private final String password;
}
