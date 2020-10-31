package com.cs.webservice.handler.auth;

import com.cs.webservice.domain.auth.EmailCertify;
import com.cs.webservice.domain.auth.repository.EmailCertifyRepository;
import com.cs.webservice.dto.auth.SendEmail;
import com.cs.webservice.utils.Random;
import lombok.RequiredArgsConstructor;
import org.apache.http.HttpStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.RequestBody;

import javax.validation.Valid;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class EmailHandlerImpl implements EmailHandler {
    private final EmailCertifyRepository emailCertifyRepository;

    private final JavaMailSender javaMailSender;

    private static final Logger logger = LoggerFactory.getLogger(JavaMailSender.class);

    @Override
    public SendEmail.Response sendEmail(@Valid @RequestBody SendEmail.Request req, BindingResult bindingResult) {
        var resp = new SendEmail.Response();
        if (bindingResult.hasErrors()) {
            resp.setStatus(HttpStatus.SC_BAD_REQUEST);
            resp.setMessage(bindingResult.getAllErrors().toString());
            return resp;
        }

        String randomString = Random.generateAlphanumericString(8);

        Optional<EmailCertify> selectedResult = emailCertifyRepository.findByEmail(req.getEmail());
        if (selectedResult.isPresent()) {
            EmailCertify existEmail = selectedResult.get();
            if (existEmail.isUsing()) {
                resp.setStatus(HttpStatus.SC_CONFLICT);
                resp.setCode(-1001);
                resp.setMessage("that email is already used");
                return resp;
            }
            existEmail.setCertified(false);
            existEmail.setAuthCode(randomString);
            emailCertifyRepository.save(existEmail);
        } else {
            EmailCertify newEmail = EmailCertify.builder()
                    .email(req.getEmail())
                    .authCode(randomString)
                    .build();
            emailCertifyRepository.save(newEmail);
        }

        new Thread(() -> {
            try {
                SimpleMailMessage message = new SimpleMailMessage();
                message.setFrom("richimous0719@gmail.com");
                message.setTo(req.getEmail());
                message.setSubject("캠페인쉐어 계정 생성을 위한 이메일 인증 코드입니다.");
                message.setText("인증 코드: " + randomString);
                javaMailSender.send(message);
            } catch (Exception e) {
                logger.error("Can't send email... " + e.getMessage(), e);
            }
        }).start();

        resp.setStatus(HttpStatus.SC_OK);
        resp.setMessage("succeed to register sending email to certify");
        return resp;
    }
}
