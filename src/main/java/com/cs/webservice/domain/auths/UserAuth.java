package com.cs.webservice.domain.auths;

import lombok.*;

import javax.persistence.*;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Getter
@Entity
@Table(name = "user_auths")
public class UserAuth {
    @Id
    @Column(unique = true, nullable = false, columnDefinition = "CHAR(17)", length = 17, name = "uuid")
    @Size(min = 17, max = 17) @NotNull @NotEmpty
    private String uuid;

    @Column(unique = true, nullable = false, length = 20, name = "user_id")
    @Size(min = 4, max = 20) @NotNull @NotEmpty
    private String userID;

    @Column(nullable = false, length = 100, name = "user_pw")
    @NotNull
    private String userPW;

    @Column(nullable = false, columnDefinition = "BOOLEAN DEFAULT FALSE", name = "locked")
    private boolean locked;
}
