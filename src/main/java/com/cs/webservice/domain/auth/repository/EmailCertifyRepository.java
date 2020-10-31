package com.cs.webservice.domain.auth.repository;

import com.cs.webservice.domain.auths.EmailCertify;
import org.springframework.data.jpa.repository.JpaRepository;

public interface EmailCertifyRepository extends JpaRepository<EmailCertify, String> {
}
