package com.cs.webservice.handler.auth;

import com.cs.webservice.domain.auth.EmailCertify;
import com.cs.webservice.domain.auth.repository.EmailCertifyRepository;
import com.cs.webservice.dto.auth.SendEmail;
import com.cs.webservice.dto.auth.VerifyEmail;
import com.cs.webservice.utils.Random;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
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
    public ResponseEntity<SendEmail.Response> sendEmail(@Valid @RequestBody SendEmail.Request req, BindingResult bindingResult) {
        var resp = new SendEmail.Response();
        if (bindingResult.hasErrors()) {
            resp.setStatus(HttpStatus.BAD_REQUEST.value());
            resp.setMessage(bindingResult.getAllErrors().toString());
            return new ResponseEntity<>(resp, HttpStatus.BAD_REQUEST);
        }

        String randomString = Random.generateAlphanumericString(8);

        Optional<EmailCertify> selectedResult = emailCertifyRepository.findByEmail(req.getEmail());
        if (selectedResult.isPresent()) {
            EmailCertify existEmail = selectedResult.get();
            if (existEmail.isUsing()) {
                resp.setStatus(HttpStatus.CONFLICT.value());
                resp.setCode(-1001);
                resp.setMessage("that email is already used");
                return new ResponseEntity<>(resp, HttpStatus.CONFLICT);
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

        resp.setStatus(HttpStatus.OK.value());
        resp.setMessage("succeed to register sending email to certify");
        return new ResponseEntity<>(resp, HttpStatus.OK);
    }

    @Override
    public ResponseEntity<VerifyEmail.Response> verifyEmail(@Valid @RequestBody VerifyEmail.Request req, BindingResult bindingResult) {
        var resp = new VerifyEmail.Response();
        if (bindingResult.hasErrors()) {
            resp.setStatus(HttpStatus.BAD_REQUEST.value());
            resp.setMessage(bindingResult.getAllErrors().toString());
            return new ResponseEntity<>(resp, HttpStatus.BAD_REQUEST);
        }

        Optional<EmailCertify> selectResult = emailCertifyRepository.findByEmail(req.getEmail());
        if (selectResult.isEmpty()) {
            resp.setStatus(HttpStatus.CONFLICT.value());
            resp.setCode(-1011);
            resp.setMessage("that email doesn't request authentication code");
            return new ResponseEntity<>(resp, HttpStatus.CONFLICT);
        }

        EmailCertify existEmail = selectResult.get();
        if (existEmail.isCertified()) {
            resp.setStatus(HttpStatus.CONFLICT.value());
            resp.setCode(-1012);
            resp.setMessage("that email was already authenticated");
            return new ResponseEntity<>(resp, HttpStatus.CONFLICT);
        }

        if (!existEmail.getAuthCode().equals(req.getAuthCode())) {
            resp.setStatus(HttpStatus.CONFLICT.value());
            resp.setCode(-1013);
            resp.setMessage("that is an incorrect email authentication code");
            return new ResponseEntity<>(resp, HttpStatus.CONFLICT);
        }

        existEmail.setCertified(true);
        emailCertifyRepository.save(existEmail);

        resp.setStatus(HttpStatus.OK.value());
        resp.setMessage("succeed to certify email");
        return new ResponseEntity<>(resp, HttpStatus.OK);
    }
}
