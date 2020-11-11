package com.cs.webservice.controller.auth;

import com.cs.webservice.dto.auth.*;
import com.cs.webservice.handler.auth.AuthHandlerImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.io.IOException;

@RestController
@RequiredArgsConstructor
@RequestMapping(value = "/v1")
public class AuthController {
    private final AuthHandlerImpl authHandler;

    @PostMapping(path = "/users", consumes = {"multipart/form-data"})
    public ResponseEntity<CreateNewUser.Response> createNewUser(@Valid @ModelAttribute CreateNewUser.Request req, BindingResult bindingResult) throws IOException {
        return authHandler.createNewUser(req, bindingResult);
    }

    @PostMapping(path = "/login/user", consumes = {"application/json"})
    public ResponseEntity<LoginUserAuth.Response> loginUserAuth(@Valid @RequestBody LoginUserAuth.Request req, BindingResult bindingResult) {
        return authHandler.loginUserAuth(req, bindingResult);
    }

    @PutMapping(path = "/users/uuid/{user_uuid}/password", consumes = {"application/json"})
    public ResponseEntity<ChangeUserPW.Response> changeUserPW(@Valid @RequestBody ChangeUserPW.Request req, BindingResult bindingResult,
                                               @RequestHeader(value = "Authorization", required = false) String token,
                                               @PathVariable("user_uuid") String userUUID) {
        return authHandler.changeUserPW(req, bindingResult, token, userUUID);
    }

    @GetMapping(path = "/users/uuid/{user_uuid}")
    public ResponseEntity<GetUserInform.Response> getUserInform(@RequestHeader(value = "Authorization", required = false) String token,
                                                @PathVariable("user_uuid") String userUUID) {
        return authHandler.getUserInform(token, userUUID);
    }

    @DeleteMapping(path = "/users/uuid/{user_uuid}")
    public ResponseEntity<DeleteUser.Response> deleteUser(@RequestHeader(value = "Authorization", required = false) String token,
                                          @PathVariable("user_uuid") String userUUID) {
        return authHandler.deleteUser(token, userUUID);
    }

    @PatchMapping(path = "/users/uuid/{user_uuid}", consumes = {"multipart/form-data"})
    public ResponseEntity<ChangeUserInform.Response> changeUserInform(@Valid @ModelAttribute ChangeUserInform.Request req, BindingResult bindingResult,
                                                      @RequestHeader(value = "Authorization", required = false) String token,
                                                      @PathVariable("user_uuid") String userUUID) throws IOException {
        return authHandler.changeUserInform(req, bindingResult, token, userUUID);
    }
}
