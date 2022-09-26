package com.github.bottomlessarchive.loa.web.view.user.controller;

import com.github.bottomlessarchive.loa.user.service.UserEntityFactory;
import com.github.bottomlessarchive.loa.web.view.user.response.InfoResponse;
import com.github.bottomlessarchive.loa.web.view.user.request.LoginRequest;
import com.github.bottomlessarchive.loa.web.view.user.response.LoginResponse;
import com.github.bottomlessarchive.loa.web.view.user.response.domain.LoginResult;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpSession;
import java.util.Optional;
import java.util.UUID;

@RestController
@RequestMapping("/user")
@RequiredArgsConstructor
public class UserController {

    private final UserEntityFactory userEntityFactory;

    @PostMapping("/login")
    public LoginResponse login(@RequestBody final LoginRequest loginRequest, final HttpSession httpSession) {
        return userEntityFactory.getUserEntity(loginRequest.getUsername(), loginRequest.getPassword())
                .map(userEntity -> {
                    httpSession.setAttribute("USER_ID", userEntity.getId().toString());

                    return LoginResponse.builder()
                            .result(LoginResult.SUCCESSFUL)
                            .build();
                })
                .orElse(
                        LoginResponse.builder()
                                .result(LoginResult.INVALID_CREDENTIALS)
                                .build()
                );
    }

    @PostMapping("/logout")
    public void logout(final HttpSession httpSession) {
        httpSession.invalidate();
    }

    @GetMapping("/info")
    public InfoResponse info(final HttpSession httpSession) {
        return Optional.ofNullable((String) httpSession.getAttribute("USER_ID"))
                .flatMap(sessionAttribute -> userEntityFactory.getUserEntity(UUID.fromString(sessionAttribute)))
                .map(userEntity ->
                        InfoResponse.builder()
                                .isLoggedIn(true)
                                .name(userEntity.getName())
                                .build()
                )
                .orElse(
                        InfoResponse.builder()
                                .isLoggedIn(false)
                                .build()
                );
    }
}
