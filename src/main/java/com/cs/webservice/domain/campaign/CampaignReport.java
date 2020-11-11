package com.cs.webservice.domain.campaign;

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
@Getter
@Setter
@Entity(name = "campaign_reports")
@Table(name = "campaign_reports")
public class CampaignReport {
    @Id
    @Column(unique = true, nullable = false, columnDefinition = "CHAR(19)", length = 19, name = "uuid")
    @Size(min = 19, max = 19) @NotNull @NotEmpty @Pattern(regexp = "^report-\\d{12}")
    private String uuid;

    @Column(columnDefinition = "CHAR(17)", length = 17, name = "reporter_uuid")
    @Size(min = 17, max = 17) @Pattern(regexp = "^user-\\d{12}")
    private String reporterUUID;

    @Column(columnDefinition = "CHAR(21)", length = 21, name = "target_uuid")
    @Size(min = 21, max = 21) @Pattern(regexp = "^campaign-\\d{12}")
    private String targetUUID;

    @Column(nullable = false, length = 20, name = "field")
    @Size(max = 20) @NotNull
    private String field;

    @Column(nullable = false, length = 50, name = "reason")
    @Size(max = 50) @NotNull
    private String reason;

    @Builder
    public CampaignReport(String uuid, String reporterUUID, String targetUUID, String field, String reason) {
        this.uuid = uuid;
        this.reporterUUID = reporterUUID;
        this.targetUUID = targetUUID;
        this.field = field;
        this.reason = reason;
    }
}
