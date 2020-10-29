package com.cs.webservice.domain.auths;

import com.cs.webservice.domain.BaseTimeEntity;
import lombok.*;

import javax.persistence.*;
import javax.validation.constraints.*;

@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Getter
@Setter
@Entity
@Table(name = "user_informs")
public class UserInform extends BaseTimeEntity {
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

    @Column(nullable = false, length = 30, name = "email")
    @Size(max = 30) @NotNull @NotEmpty @Email
    private String email;

    @Builder
    public UserInform(String name, String nickName, String email) {
        this.userAuth = new UserAuth();
        this.name = name;
        this.nickName = nickName;
        this.email = email;
    }
}
