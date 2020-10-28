package com.cs.webservice.domain;

import com.cs.webservice.domain.auths.repository.UserAuthRepository;
import org.junit.After;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;


@RunWith(SpringRunner.class)
@SpringBootTest
public class UserAuthRepositoryTest {
    @Autowired
    UserAuthRepository userAuthRepository;

    @After
    public void cleanup() {
        userAuthRepository.deleteAll();
    }
}
