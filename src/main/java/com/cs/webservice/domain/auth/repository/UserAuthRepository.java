package com.cs.webservice.domain.auths.repository;

import com.cs.webservice.domain.auths.UserAuth;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserAuthRepository extends JpaRepository<UserAuth, String> {
}
