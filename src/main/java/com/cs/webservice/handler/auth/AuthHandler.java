package com.cs.webservice.handler.auth;

import com.cs.webservice.dto.auth.*;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;

import javax.validation.Valid;
import java.io.IOException;

public interface AuthHandler {
    CreateNewUser.Response createNewUser(CreateNewUser.Request req, BindingResult bindingResult) throws IOException;
    LoginUserAuth.Response loginUserAuth(LoginUserAuth.Request req, BindingResult bindingResult);
    ChangeUserPW.Response changeUserPW(ChangeUserPW.Request req, BindingResult bindingResult, String token, String userUUID);
    GetUserInform.Response getUserInform(String token, String userUUID);
    DeleteUser.Response deleteUser(String token, String userUUID);
    ChangeUserInform.Response changeUserInform(ChangeUserInform.Request req, BindingResult bindingResult, String token, String userUUID);
}
