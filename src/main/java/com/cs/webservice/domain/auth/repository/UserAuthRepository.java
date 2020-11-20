package com.cs.webservice.domain.auth.repository;

import com.cs.webservice.domain.auth.UserAuth;
import com.cs.webservice.utils.Random;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserAuthRepository extends JpaRepository<UserAuth, String> {
    Optional<UserAuth> findByUserID(String userID);

    default String getAvailableUUID() {
        while (true) {
            String userUUID = "user-" + Random.generateNumberString(12);
            if (this.findById(userUUID).isEmpty()) {
                return userUUID;
            }
        }
    }
}
