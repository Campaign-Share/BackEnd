package com.cs.webservice.domain.campaign;

import com.cs.webservice.domain.BaseTimeEntity;
import lombok.*;

import javax.persistence.*;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;

// table campaign_votes {
//  voter_uuid int [ref: > user_auths.uuid]
//  campaign_uuid int [ref: > campaigns.uuid]
//  agree boolean
//}

@NoArgsConstructor(access = AccessLevel.PUBLIC)
@AllArgsConstructor
@Getter
@Setter
@Entity(name = "campaign_votes")
@Table(name = "campaign_votes")
public class CampaignVote extends BaseTimeEntity {
    @Id
    @GeneratedValue
    private Long id;

    @Column(nullable = false, columnDefinition = "CHAR(17)", length = 17, name = "voter_uuid")
    @Size(min = 17, max = 17) @NotNull @NotEmpty @Pattern(regexp = "^user-\\d{12}")
    private String voterUUID;

    @Column(nullable = false, columnDefinition = "CHAR(21)", length = 21, name = "campaign_uuid")
    @Size(min = 21, max = 21) @NotNull @NotEmpty @Pattern(regexp = "^campaign-\\d{12}")
    private String campaignUUID;

    @Column(nullable = false, name = "agree")
    private boolean agree;

    @Builder
    public CampaignVote(String voterUUID, String campaignUUID, boolean agree) {
        this.voterUUID = voterUUID;
        this.campaignUUID = campaignUUID;
        this.agree = agree;
    }
}
