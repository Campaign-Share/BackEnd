package com.cs.webservice.domain.auth;

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

    @Column(nullable = false, length = 30, name = "email", unique = true)
    @Size(max = 30) @NotNull @NotEmpty @Email
    private String email;

    @Column(length = 100, name = "profile_uri")
    @Size(max = 100)
    private String profileURI;

    @Column(nullable = false, columnDefinition = "INT(11) DEFAULT 0", name = "approved_number")
    private int approvedNumber;

    @Column(nullable = false, columnDefinition = "INT(11) DEFAULT 0", name = "rejected_number")
    private int rejectedNumber;

    @Column(nullable = false, columnDefinition = "INT(11) DEFAULT 0", name = "participation_number")
    private int participationNumber;

    @Builder
    public UserInform(String name, String nickName, String email) {
        this.userAuth = new UserAuth();
        this.name = name;
        this.nickName = nickName;
        this.email = email;
    }
}
