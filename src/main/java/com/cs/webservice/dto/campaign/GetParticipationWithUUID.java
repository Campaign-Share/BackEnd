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
        private String participationUUID;

        @JsonProperty("participant_uuid")
        private String participantUUID;

        @JsonProperty("campaign_uuid")
        private String campaignUUID;

        @JsonProperty("introduction")
        private String introduction;

        @JsonProperty("state")
        private String state;

        @JsonProperty("evidence_uri")
        private String evidenceURI;

        @JsonProperty("user_name")
        private String userName;

        @JsonProperty("campaign_title")
        private String campaignTitle;
    }
}
