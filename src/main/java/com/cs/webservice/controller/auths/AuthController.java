package com.cs.webservice.controller.auths;

import com.cs.webservice.domain.auths.repository.UserAuthRepository;
import com.cs.webservice.dto.auths.CreateNewUser;
import lombok.AllArgsConstructor;
import org.apache.http.HttpStatus;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;

@RestController
@AllArgsConstructor
public class AuthController {
    private UserAuthRepository userAuthRepository;

    @PostMapping(value = "/v1/users", consumes = {"application/json"})
    public CreateNewUser.Response createNewUser(@Valid @RequestBody CreateNewUser.Request dto, BindingResult bindingResult) {
        var resp = new CreateNewUser.Response();
        if (bindingResult.hasErrors()) {
            resp.setStatus(HttpStatus.SC_BAD_REQUEST);
            resp.setMessage(bindingResult.getAllErrors().toString());
            return resp;
        }

        System.out.println(dto);
        resp.setStatus(HttpStatus.SC_OK);
        return resp;
    }
}