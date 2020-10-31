package com.cs.webservice.handler.auth;

import com.cs.webservice.domain.auth.repository.EmailCertifyRepository;
import com.cs.webservice.dto.auth.SendEmail;
import com.cs.webservice.utils.Random;
import lombok.RequiredArgsConstructor;
import org.apache.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.RequestBody;

import javax.validation.Valid;

@Service
@RequiredArgsConstructor
public class EmailHandlerImpl implements EmailHandler {
    private final EmailCertifyRepository emailCertifyRepository;

    @Override
    public SendEmail.Response sendEmail(@Valid @RequestBody SendEmail.Request req, BindingResult bindingResult) {
        var resp = new SendEmail.Response();
        if (bindingResult.hasErrors()) {
            resp.setStatus(HttpStatus.SC_BAD_REQUEST);
            resp.setMessage(bindingResult.getAllErrors().toString());
            return resp;
        }

        String randomString = Random.generateAlphanumericString(8);

        resp.setStatus(HttpStatus.SC_OK);
        resp.setMessage("succeed to send email to certify");
        return resp;
    }
}
