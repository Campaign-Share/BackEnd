package com.cs.webservice.controller.auth;

import com.cs.webservice.dto.auth.CreateNewUser;
import com.cs.webservice.dto.auth.LoginUserAuth;
import com.cs.webservice.handler.auth.AuthHandlerImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

@RestController
@RequiredArgsConstructor
@RequestMapping(value = "/v1")
public class AuthController {
    private final AuthHandlerImpl authHandler;

    @PostMapping(path = "/users", consumes = {"application/json"})
    public CreateNewUser.Response createNewUser(@Valid @RequestBody CreateNewUser.Request req, BindingResult bindingResult) {
        return authHandler.createNewUser(req, bindingResult);
    }

    @PostMapping(path = "/login/user", consumes = {"application/json"})
    public LoginUserAuth.Response loginUserAuth(@Valid @RequestBody LoginUserAuth.Request req, BindingResult bindingResult) {
        return authHandler.loginUserAuth(req, bindingResult);
    }
}