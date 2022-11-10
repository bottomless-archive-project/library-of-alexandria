package com.github.bottomlessarchive.loa.web.view.user.response;

import com.github.bottomlessarchive.loa.web.view.user.response.domain.LoginResult;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class LoginResponse {

    private final LoginResult result;
}
