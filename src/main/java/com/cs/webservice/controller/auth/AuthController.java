package com.cs.webservice.controller.auth;

import com.cs.webservice.dto.auth.ChangeUserPW;
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

    @PutMapping(path = "/users/uuid/{user_uuid}/password", consumes = {"application/json"})
    public ChangeUserPW.Response loginUserAuth(@RequestBody ChangeUserPW.Request req, @Valid BindingResult bindingResult,
                                               @RequestHeader(value = "Authorization", required = false) String token,
                                               @PathVariable("user_uuid") String userUUID) {
        return authHandler.changeUserPW(req, bindingResult, token, userUUID);
    }
}
