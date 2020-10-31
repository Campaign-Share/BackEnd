package com.cs.webservice.controller.auths;

import com.cs.webservice.domain.auths.repository.EmailCertifyRepository;
import com.cs.webservice.dto.auths.SendEmail;
import com.cs.webservice.utils.Random;
import lombok.AllArgsConstructor;
import org.apache.http.HttpStatus;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;

@RestController
@AllArgsConstructor
public class EmailController {
    private EmailCertifyRepository emailCertifyRepository;

    @PostMapping(value = "/v1/emails", consumes = {"application/json"})
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
