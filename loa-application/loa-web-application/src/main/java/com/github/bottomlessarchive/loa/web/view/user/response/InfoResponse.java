package com.github.bottomlessarchive.loa.web.view.user.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class InfoResponse {

    private final String name;
    private final boolean isLoggedIn;
}
