package com.cs.webservice.domain.auths;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import javax.validation.constraints.*;

@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Getter
@Entity
@Table(name = "user_informs")
public class UserInform {
    @Id
    @GeneratedValue
    private Long id;

    @OneToOne
    @JoinColumn(name = "user_uuid", referencedColumnName = "uuid")
    private UserAuth userAuth;

    @Column(nullable = false, length = 5, name = "name")
    @Size(min = 2, max = 5) @NotNull @NotEmpty
    private String name;

    @Column(nullable = false, length = 10, name = "nick_name")
    @Size(max = 10) @NotNull
    private String nickName;

    @Column(nullable = false, length = 11, name = "phone_number", columnDefinition = "CHAR(11)")
    @Size(min = 11, max = 11) @NotNull @NotEmpty
    @Pattern(regexp = "^010\\d{8}")
    private String phoneNumber;
}
