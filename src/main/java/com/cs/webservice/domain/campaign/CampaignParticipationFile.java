package com.cs.webservice.domain.campaign;

// table campaign_participation_files {
//  participation_uuid int [ref: > campaign_participations.uuid]
//  file_uri varchar(100)
//}

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
@Entity(name = "campaign_participation_files")
@Table(name = "campaign_participation_files")
public class CampaignParticipationFile {
    @Id
    @GeneratedValue
    private Long id;

    @Column(nullable = false, columnDefinition = "CHAR(26)", length = 26, name = "participation_uuid")
    @Size(min = 26, max = 26) @NotNull @NotEmpty @Pattern(regexp = "^participation-\\d{12}")
    private String participationUUID;

    @Column(nullable = false, length = 100, name = "file_uri")
    @Size(max = 100) @NotNull
    private String fileURI;
}
