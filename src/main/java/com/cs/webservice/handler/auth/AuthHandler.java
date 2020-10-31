package com.cs.webservice.handler.auth;

import com.cs.webservice.dto.auth.CreateNewUser;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.RequestBody;

import javax.validation.Valid;

public interface AuthHandler {
    CreateNewUser.Response createNewUser(@Valid @RequestBody CreateNewUser.Request dto, BindingResult bindingResult);
}
