package com.cs.webservice.domain.auth.repository;

import com.cs.webservice.domain.auth.UserAuth;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserAuthRepository extends JpaRepository<UserAuth, String> {
}
