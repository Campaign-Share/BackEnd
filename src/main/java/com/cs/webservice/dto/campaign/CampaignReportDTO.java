package com.cs.webservice.dto.campaign;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Setter;

@Setter
@Builder
public class CampaignReportDTO {
    @JsonProperty("report_uuid")
    private String reportUUID;

    @JsonProperty("reporter_uuid")
    private String reporterUUID;

    @JsonProperty("target_uuid")
    private String targetUUID;

    @JsonProperty("field")
    private String field;

    @JsonProperty("reason")
    private String reason;

    @JsonProperty("state")
    private String state;
}
