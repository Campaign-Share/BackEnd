package com.cs.webservice.domain.auths;

import com.cs.webservice.domain.BaseTimeEntity;
import lombok.*;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.validation.constraints.Email;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Getter
@Entity
@Table(name = "email_certifies")
public class EmailCertify extends BaseTimeEntity {
    @Id
    @Column(unique = true, nullable = false, length = 30, name = "email")
    @Size(max = 30) @NotNull @NotEmpty @Email
    private String email;

    @Column(nullable = false, length = 8, columnDefinition = "CHAR(8)", name = "auth_code")
    @Size(min = 8, max = 8) @NotNull @NotEmpty
    private String authCode;

    @Column(nullable = false, columnDefinition = "BOOLEAN DEFAULT FALSE", name = "certified")
    private boolean certified;

    @Column(nullable = false, columnDefinition = "BOOLEAN DEFAULT FALSE", name = "is_using")
    private boolean isUsing;

    @Builder
    public EmailCertify(String email, String authCode) {
        this.email = email;
        this.authCode = authCode;
    }
}
