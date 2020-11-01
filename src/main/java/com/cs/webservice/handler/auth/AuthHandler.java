package com.cs.webservice.handler.auth;

import com.cs.webservice.dto.auth.ChangeUserPW;
import com.cs.webservice.dto.auth.CreateNewUser;
import com.cs.webservice.dto.auth.LoginUserAuth;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;

import javax.validation.Valid;

public interface AuthHandler {
    CreateNewUser.Response createNewUser(@Valid @RequestBody CreateNewUser.Request req, BindingResult bindingResult);
    LoginUserAuth.Response loginUserAuth(@Valid @RequestBody LoginUserAuth.Request req, BindingResult bindingResult);
    ChangeUserPW.Response changeUserPW(ChangeUserPW.Request req, BindingResult bindingResult, String token, String userUUID);
}
