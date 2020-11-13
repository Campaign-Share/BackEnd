package com.cs.webservice.handler.auth;

import com.cs.webservice.dto.auth.*;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;

import javax.validation.Valid;
import java.io.IOException;

public interface AuthHandler {
    ResponseEntity<CreateNewUser.Response> createNewUser(CreateNewUser.Request req, BindingResult bindingResult) throws IOException;
    ResponseEntity<LoginUserAuth.Response> loginUserAuth(LoginUserAuth.Request req, BindingResult bindingResult);
    ResponseEntity<ChangeUserPW.Response> changeUserPW(ChangeUserPW.Request req, BindingResult bindingResult, String token, String userUUID);
    ResponseEntity<GetUserInform.Response> getUserInform(String token, String userUUID);
    ResponseEntity<DeleteUser.Response> deleteUser(String token, String userUUID);
    ResponseEntity<ChangeUserInform.Response> changeUserInform(ChangeUserInform.Request req, BindingResult bindingResult, String token, String userUUID) throws IOException;
    ResponseEntity<LoginAdminAuth.Response> loginAdminAuth(LoginAdminAuth.Request req, BindingResult bindingResult);
    ResponseEntity<GetUserInformsWithUUIDs.Response> getUserInformsWithUUIDs(GetUserInformsWithUUIDs.Request req, BindingResult bindingResult, String token);
}
