package com.cs.webservice.controller.auth;

import com.cs.webservice.domain.auth.repository.EmailCertifyRepository;
import com.cs.webservice.dto.auth.SendEmail;
import com.cs.webservice.dto.auth.VerifyEmail;
import com.cs.webservice.handler.auth.EmailHandlerImpl;
import com.cs.webservice.utils.Random;
import lombok.AllArgsConstructor;
import org.apache.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;

@RestController
@AllArgsConstructor
public class EmailController {
    private final EmailHandlerImpl emailHandler;

    @PostMapping(value = "/v1/email/verify", consumes = {"application/json"})
    public ResponseEntity<SendEmail.Response> sendEmail(@Valid @RequestBody SendEmail.Request req, BindingResult bindingResult) {
        return emailHandler.sendEmail(req, bindingResult);
    }

    @PutMapping(value = "/v1/email/verify", consumes = {"application/json"})
    public ResponseEntity<VerifyEmail.Response> verifyEmail(@Valid @RequestBody VerifyEmail.Request req, BindingResult bindingResult) {
        return emailHandler.verifyEmail(req, bindingResult);
    }
}
