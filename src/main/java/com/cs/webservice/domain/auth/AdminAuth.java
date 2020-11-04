package com.cs.webservice.domain.auth;

import com.cs.webservice.domain.BaseTimeEntity;
import lombok.*;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;

@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Getter
@Setter
@Entity(name = "admin_auths")
@Table(name = "admin_auths")
public class AdminAuth extends BaseTimeEntity {
    @Id
    @Column(unique = true, nullable = false, columnDefinition = "CHAR(18)", length = 18, name = "uuid")
    @Size(min = 18, max = 18) @NotNull
    @NotEmpty
    @Pattern(regexp = "^admin-\\d{12}")
    private String uuid;

    @Column(unique = true, nullable = false, length = 20, name = "user_id")
    @Size(min = 4, max = 20) @NotNull @NotEmpty
    private String adminID;

    @Column(nullable = false, length = 100, name = "user_pw")
    @NotNull
    private String adminPW;

    @Column(nullable = false, columnDefinition = "BOOLEAN DEFAULT FALSE", name = "locked")
    private boolean locked;

    @Builder
    public AdminAuth(String uuid, String userId, String userPW) {
        this.uuid = uuid;
        this.adminID = userId;
        this.adminPW = userPW;
    }
}
