package com.cs.webservice.domain.auth.repository;

import com.cs.webservice.domain.auth.EmailCertify;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface EmailCertifyRepository extends JpaRepository<EmailCertify, String> {
    Optional<EmailCertify> findByEmail(String email);
    Optional<EmailCertify> findByEmailAndCertified(String email, Boolean certified);
}
