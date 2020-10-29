package com.cs.webservice.domain.auths.repository;

import com.cs.webservice.domain.auths.UserAuth;
import com.cs.webservice.domain.auths.UserInform;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserInformRepository  extends JpaRepository<UserInform, String> {
}
