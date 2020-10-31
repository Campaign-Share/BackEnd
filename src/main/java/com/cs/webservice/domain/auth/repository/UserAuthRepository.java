package com.cs.webservice.domain.auth.repository;

import com.cs.webservice.domain.auth.UserAuth;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserAuthRepository extends JpaRepository<UserAuth, String> {
    Optional<UserAuth> findByUserID(String userID);
}
