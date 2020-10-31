package com.cs.webservice.handler.auth;

import com.cs.webservice.domain.auth.repository.UserAuthRepository;
import com.cs.webservice.dto.auth.CreateNewUser;
import lombok.RequiredArgsConstructor;
import org.apache.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.RequestBody;

import javax.validation.Valid;

@Service
@RequiredArgsConstructor
public class AuthHandlerImpl implements AuthHandler {
    private final UserAuthRepository userAuthRepository;

    @Override
    public CreateNewUser.Response createNewUser(@Valid @RequestBody CreateNewUser.Request req, BindingResult bindingResult) {
        var resp = new CreateNewUser.Response();
        if (bindingResult.hasErrors()) {
            resp.setStatus(HttpStatus.SC_BAD_REQUEST);
            resp.setMessage(bindingResult.getAllErrors().toString());
            return resp;
        }

        System.out.println(req);
        resp.setStatus(HttpStatus.SC_OK);
        return resp;
    }
}
