package com.cs.webservice.domain.campaign;

import lombok.*;

import javax.persistence.*;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;
import java.util.List;

@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Getter
@Setter
@Entity(name = "campaign_participations")
@Table(name = "campaign_participations")
public class CampaignParticipation {
    @Id
    @Column(unique = true, nullable = false, columnDefinition = "CHAR(26)", length = 26, name = "uuid")
    @Size(min = 26, max = 26) @NotNull @NotEmpty @Pattern(regexp = "^participation-\\d{12}")
    private String uuid;

    @Column(columnDefinition = "CHAR(17)", length = 17, name = "participant_uuid")
    @Size(min = 17, max = 17) @Pattern(regexp = "^user-\\d{12}")
    private String participantUUID;

    @Column(columnDefinition = "CHAR(21)", length = 21, name = "campaign_uuid")
    @Size(min = 21, max = 21) @Pattern(regexp = "^campaign-\\d{12}")
    private String campaignUUID;

    @Column(nullable = false, length = 300, name = "information") // 길이 미정
    @Size(max = 300) @NotNull
    private String information;

    @Column(nullable = false, columnDefinition = "BOOLEAN DEFAULT FALSE", name = "accepted")
    private boolean accepted;

    @Column(columnDefinition = "CHAR(18)", length = 18, name = "accepter_uuid")
    @Size(min = 18, max = 18) @Pattern(regexp = "^admin-\\d{12}")
    private String accepterUUID;

    @OneToMany(mappedBy = "participationUUID", cascade = CascadeType.ALL)
    private List<CampaignParticipationFile> campaignParticipationFiles;
}
