package com.cs.webservice.domain;

import com.cs.webservice.domain.auths.UserAuth;
import com.cs.webservice.domain.auths.repository.UserAuthRepository;
import org.joda.time.LocalDateTime;
import org.junit.After;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.springframework.test.util.AssertionErrors.assertTrue;

@RunWith(SpringRunner.class)
@SpringBootTest
public class UserAuthRepositoryTest {
    @Autowired
    UserAuthRepository userAuthRepository;

    @After
    public void cleanup() {
        userAuthRepository.deleteAll();
    }

    @Test
    public void UserAuthSaveAndLoad() {
        LocalDateTime now = LocalDateTime.now();
        userAuthRepository.save(UserAuth.builder()
                .uuid("user-123412341234")
                .userId("jinhong0719")
                .userPW("testPW")
                .build());

        List<UserAuth> authsList = userAuthRepository.findAll();

        UserAuth auths = authsList.get(0);
        assertEquals(auths.getUuid(), "user-123412341234");
        assertEquals(auths.getUserID(), "jinhong0719");
        assertEquals(auths.getUserPW(), "testPW");

        assertTrue("error", auths.getCreatedAt().isAfter(now));
        assertTrue("error", auths.getUpdatedAt().isAfter(now));
    }
}
