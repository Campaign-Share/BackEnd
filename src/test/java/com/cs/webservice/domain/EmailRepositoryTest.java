package com.cs.webservice.domain;

import com.cs.webservice.domain.auth.EmailCertify;
import com.cs.webservice.domain.auth.repository.EmailCertifyRepository;
import org.junit.After;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.springframework.test.util.AssertionErrors.assertTrue;

@RunWith(SpringRunner.class)
@SpringBootTest
public class EmailRepositoryTest {
    @Autowired
    EmailCertifyRepository emailCertifyRepository;

    @After
    public void cleanup() {
        emailCertifyRepository.deleteAll();
    }

    @Test
    public void EmailCertifyAuthSaveAndLoad() {
        LocalDateTime now = LocalDateTime.now();
        EmailCertify emailCertify = EmailCertify.builder()
                .email("jinhong0719@naver.com")
                .authCode("a1b2c3d4")
                .build();
        emailCertifyRepository.save(emailCertify);

        List<EmailCertify> emailCertifyList = emailCertifyRepository.findAll();

        EmailCertify certify = emailCertifyList.get(0);

        assertEquals(certify.getEmail(), "jinhong0719@naver.com");
        assertEquals(certify.getAuthCode(), "a1b2c3d4");
        assertFalse(certify.isCertified());
        assertFalse(certify.isUsing());
        assertTrue("error", certify.getCreatedAt().isAfter(now));
        assertTrue("error", certify.getUpdatedAt().isAfter(now));
    }
}
