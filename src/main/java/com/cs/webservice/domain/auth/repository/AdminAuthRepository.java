package com.cs.webservice.domain.auth.repository;

import com.cs.webservice.domain.auth.AdminAuth;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AdminAuthRepository extends JpaRepository<AdminAuth, String> {
}
