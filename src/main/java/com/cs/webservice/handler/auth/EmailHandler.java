package com.cs.webservice.handler.auth;

import com.cs.webservice.dto.auth.SendEmail;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.RequestBody;

import javax.validation.Valid;

public interface EmailHandler {
    SendEmail.Response sendEmail(@Valid @RequestBody SendEmail.Request req, BindingResult bindingResult);
}