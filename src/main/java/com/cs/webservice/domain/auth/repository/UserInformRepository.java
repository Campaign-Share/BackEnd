package com.cs.webservice.domain.auth.repository;

import com.cs.webservice.domain.auth.UserAuth;
import com.cs.webservice.domain.auth.UserInform;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface UserInformRepository extends JpaRepository<UserInform, Long> {
    Optional<UserInform> findByEmail(String email);
    Optional<UserInform> findByUserAuth(UserAuth userAuth);

    @Query(value = "SELECT * FROM user_informs ORDER BY participation_number DESC LIMIT ?2 OFFSET ?1", nativeQuery = true)
    List<UserInform> findAllWithPagingSortedByParticipationNumber(Integer start, Integer count);
}
