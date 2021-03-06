package com.cs.webservice.domain.auth;

import com.cs.webservice.domain.BaseTimeEntity;
import com.cs.webservice.domain.campaign.Campaign;
import com.cs.webservice.domain.campaign.CampaignParticipation;
import com.cs.webservice.domain.campaign.CampaignReport;
import com.cs.webservice.domain.campaign.CampaignVote;
import lombok.*;

import javax.persistence.*;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;
import java.time.LocalDate;
import java.util.List;
import java.util.Set;

@NoArgsConstructor(access = AccessLevel.PUBLIC)
@AllArgsConstructor
@Getter
@Setter
@Entity(name = "user_auths")
@Table(name = "user_auths")
public class UserAuth extends BaseTimeEntity {
    @Id
    @Column(unique = true, nullable = false, columnDefinition = "CHAR(17)", length = 17, name = "uuid")
    @Size(min = 17, max = 17) @NotNull @NotEmpty @Pattern(regexp = "^user-\\d{12}")
    private String uuid;

    @Column(unique = true, nullable = false, length = 20, name = "user_id")
    @Size(min = 4, max = 20) @NotNull @NotEmpty
    private String userID;

    @Column(nullable = false, length = 100, name = "user_pw")
    @NotNull
    private String userPW;

    @Column(nullable = false, columnDefinition = "BOOLEAN DEFAULT FALSE", name = "locked")
    private boolean locked;

    @Column(name = "lock_period")
    private LocalDate lockPeriod;

    @Column(nullable = false, columnDefinition = "INT(11) DEFAULT 0", name = "lock_number")
    private int lockNumber;

    @Column(nullable = false, columnDefinition = "BOOLEAN DEFAULT FALSE", name = "have_used")
    private boolean haveUsed;

    @OneToOne(mappedBy = "userAuth", cascade = CascadeType.ALL)
    @PrimaryKeyJoinColumn
    private UserInform userInform;

    @OneToMany(mappedBy = "userUUID", cascade = CascadeType.ALL)
    private List<Campaign> campaigns;

    @OneToMany(mappedBy = "participantUUID", cascade = CascadeType.ALL)
    private List<CampaignParticipation> campaignParticipations;

    @OneToMany(mappedBy = "reporterUUID", cascade = CascadeType.ALL)
    private List<CampaignReport> campaignReports;

    @OneToMany(mappedBy = "voterUUID", cascade = CascadeType.ALL)
    private List<CampaignVote> campaignVotes;

    @Builder
    public UserAuth(String uuid, String userId, String userPW) {
        this.uuid = uuid;
        this.userID = userId;
        this.userPW = userPW;
    }
}
