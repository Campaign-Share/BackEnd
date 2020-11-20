package com.cs.webservice.domain.auth.repository;

import com.cs.webservice.domain.auth.AdminAuth;
import com.cs.webservice.domain.auth.UserAuth;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface AdminAuthRepository extends JpaRepository<AdminAuth, String> {
    Optional<AdminAuth> findByAdminID(String adminID);
}
