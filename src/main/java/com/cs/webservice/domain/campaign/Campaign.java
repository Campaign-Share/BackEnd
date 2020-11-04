package com.cs.webservice.domain.campaign;

import com.cs.webservice.domain.BaseTimeEntity;
import com.cs.webservice.domain.auth.AdminAuth;
import com.cs.webservice.domain.auth.UserAuth;
import lombok.*;

import javax.persistence.*;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;
import java.time.LocalDate;
import java.util.Set;

@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Getter
@Setter
@Entity(name = "campaigns")
@Table(name = "campaigns")
public class Campaign extends BaseTimeEntity {
    @Id
    @Column(unique = true, nullable = false, columnDefinition = "CHAR(21)", length = 17, name = "uuid")
    @Size(min = 21, max = 21) @NotNull @NotEmpty @Pattern(regexp = "^campaign-\\d{12}")
    private String uuid;

    @ManyToOne
    @JoinColumn(name = "user_uuid", nullable = false, referencedColumnName = "uuid")
    @Size(min = 17, max = 17) @NotNull @NotEmpty @Pattern(regexp = "^user-\\d{12}")
    private UserAuth userAuth;

    @Column(nullable = false, columnDefinition = "BOOLEAN DEFAULT FALSE", name = "accepted")
    private Boolean accepted;

    @ManyToOne
    @JoinColumn(name = "accepter_uuid", referencedColumnName = "uuid")
    @Size(min = 18, max = 18) @Pattern(regexp = "^admin-\\d{12}")
    private AdminAuth adminAuth;

    @Column(nullable = false, length = 50, name = "title")
    @Size(max = 50) @NotNull
    private String title;

    @Column(length = 50, name = "sub_title")
    @Size(max = 50)
    private String subTitle;

    @Column(nullable = false, length = 300, name = "introduction")
    @Size(max = 300) @NotNull
    private String introduction;

    @Column(nullable = false, length = 300, name = "participation")
    @Size(max = 300) @NotNull
    private String participation;

    @Column(nullable = false, name = "start_date") @NotNull
    private LocalDate startDate;

    @Column(nullable = false, name = "end_date") @NotNull
    private LocalDate endDate;

    @Column(length = 100, name = "post_uri")
    @Size(max = 100)
    private String postURI;

    @OneToMany(mappedBy = "campaign", cascade = CascadeType.ALL)
    private Set<CampaignTag> campaignTags;

    @Builder
    public Campaign(String uuid, String userUUID, String title, String subTitle, String introduction,
                    String participation, LocalDate startDate, LocalDate endDate, String postURI) {
        this.userAuth = new UserAuth();
        this.adminAuth = new AdminAuth();

        this.uuid = uuid;
        this.userAuth.setUuid(userUUID);
        this.title = title;
        this.subTitle = subTitle;
        this.introduction = introduction;
        this.participation = participation;
        this.startDate = startDate;
        this.endDate = endDate;
        this.postURI = postURI;
    }
}
