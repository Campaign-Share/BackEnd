package com.cs.webservice.controller.auth;

import com.cs.webservice.domain.auth.repository.UserAuthRepository;
import com.cs.webservice.dto.auth.CreateNewUser;
import com.cs.webservice.handler.auth.AuthHandlerImpl;
import lombok.AllArgsConstructor;
import lombok.RequiredArgsConstructor;
import org.apache.http.HttpStatus;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;

@RestController
@RequiredArgsConstructor
public class AuthController {
    private final AuthHandlerImpl authHandler;

    @PostMapping(value = "/v1/users", consumes = {"application/json"})
    public CreateNewUser.Response createNewUser(@Valid @RequestBody CreateNewUser.Request req, BindingResult bindingResult) {
        return authHandler.createNewUser(req, bindingResult);
    }
}