package com.cs.webservice.domain.auths;

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
public class EmailCertify {
    @Id
    @Column(unique = true, nullable = false, length = 30, name = "email")
    @Size(max = 30) @NotNull @NotEmpty @Email
    private String email;

    @Column(nullable = false, length = 6, columnDefinition = "CHAR(6)", name = "auth_code")
    @Size(min = 6, max = 6) @NotNull @NotEmpty
    private String authCode;

    @Column(nullable = false, columnDefinition = "BOOLEAN DEFAULT FALSE", name = "certified")
    private boolean certified;

    @Column(nullable = false, columnDefinition = "BOOLEAN DEFAULT FALSE", name = "using")
    private boolean using;

    @Builder
    public EmailCertify(String email, String authCode) {
        this.email = email;
        this.authCode = authCode;
    }
}
