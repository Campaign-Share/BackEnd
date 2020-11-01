package com.cs.webservice.domain.auth.repository;

import com.cs.webservice.domain.auth.UserAuth;
import com.cs.webservice.domain.auth.UserInform;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserInformRepository extends JpaRepository<UserInform, Long> {
    Optional<UserInform> findByEmail(String email);
    Optional<UserInform> findByUserAuth(UserAuth userAuth);
}
