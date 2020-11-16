package com.cs.webservice.dto.campaign;


import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Setter;

@Setter
@Builder
public final class ParticipationDTO {
    @JsonProperty("participation_uuid")
    String participationUUID;

    @JsonProperty("participant_uuid")
    String participantUUID;

    @JsonProperty("campaign_uuid")
    String campaignUUID;

    @JsonProperty("introduction")
    String introduction;

    @JsonProperty("state")
    String state;

    @JsonProperty("evidence_uri")
    String evidenceURI;
}
