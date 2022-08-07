package com.github.bottomlessarchive.loa.web.view.info.controller;

import com.github.bottomlessarchive.loa.user.configuration.UserConfigurationProperties;
import com.github.bottomlessarchive.loa.web.view.info.response.InfoResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class InfoController {

    private final UserConfigurationProperties userConfigurationProperties;

    @GetMapping("/info")
    public InfoResponse info() {
        return InfoResponse.builder()
                .usersEnabled(userConfigurationProperties.enabled())
                .build();
    }
}
