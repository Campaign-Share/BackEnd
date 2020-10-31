package com.cs.webservice.domain.auth.repository;

import com.cs.webservice.domain.auth.UserInform;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserInformRepository extends JpaRepository<UserInform, Long> {
}
