package com.cs.webservice.domain.campaign;

import com.cs.webservice.domain.BaseTimeEntity;
import com.cs.webservice.domain.auth.AdminAuth;
import lombok.*;

import javax.persistence.*;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;

@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Getter
@Setter
@Entity(name = "campaign_tags")
@Table(name = "campaign_tags")
public class CampaignTag extends BaseTimeEntity {
    @Id
    @GeneratedValue
    private Long id;

    @Column(nullable = false, columnDefinition = "CHAR(21)", length = 17, name = "campaign_uuid")
    @Size(min = 21, max = 21) @NotNull @NotEmpty @Pattern(regexp = "^campaign-\\d{12}")
    private String campaignUUID;

    @Column(nullable = false, length = 20, name = "tag")
    @Size(max = 20) @NotNull
    private String tag;

    @Builder
    public CampaignTag(String campaignUUID, String tag) {
        this.campaignUUID = campaignUUID;
        this.setTag(tag);
    }
}
