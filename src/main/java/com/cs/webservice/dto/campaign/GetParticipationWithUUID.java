package com.cs.webservice.dto.campaign;

import com.cs.webservice.dto.BaseResponse;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.NoArgsConstructor;
import lombok.Setter;

public class GetParticipationWithUUID {
    @Setter
    @NoArgsConstructor
    public static class Response extends BaseResponse {
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
}
