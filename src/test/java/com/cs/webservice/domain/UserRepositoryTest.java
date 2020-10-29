package com.cs.webservice.domain;

import com.cs.webservice.domain.auths.UserAuth;
import com.cs.webservice.domain.auths.UserInform;
import com.cs.webservice.domain.auths.repository.UserAuthRepository;
import com.cs.webservice.domain.auths.repository.UserInformRepository;
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
public class UserRepositoryTest {
    @Autowired
    UserAuthRepository userAuthRepository;
    @Autowired
    UserInformRepository userInformRepository;

    @After
    public void cleanup() {
        userAuthRepository.deleteAll();
    }

    @Test
    public void UserAuthSaveAndLoad() {
        LocalDateTime now = LocalDateTime.now();
        UserAuth userAuth = UserAuth.builder()
                .uuid("user-123412341234")
                .userId("jinhong0719")
                .userPW("testPW")
                .build();
        userAuthRepository.save(userAuth);

        UserInform userInform = UserInform.builder()
                .name("박진홍")
                .nickName("첫 계정")
                .email("jinhong0719@naver.com").build();
        userInform.setUserAuth(userAuth);
        userInformRepository.save(userInform);

        List<UserAuth> authsList = userAuthRepository.findAll();
        List<UserInform> informList = userInformRepository.findAll();

        UserAuth auth = authsList.get(0);
        UserInform inform = informList.get(0);

        assertEquals(auth.getUuid(), "user-123412341234");
        assertEquals(auth.getUserID(), "jinhong0719");
        assertEquals(auth.getUserPW(), "testPW");
        assertTrue("error", auth.getCreatedAt().isAfter(now));
        assertTrue("error", auth.getUpdatedAt().isAfter(now));
        assertEquals(inform.getUserAuth().getUuid(), "user-123412341234");
        assertEquals(inform.getName(), "박진홍");
        assertEquals(inform.getNickName(), "첫 계정");
        assertEquals(inform.getEmail(), "jinhong0719@naver.com");
    }
}
