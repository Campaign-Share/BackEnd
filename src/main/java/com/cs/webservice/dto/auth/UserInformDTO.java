package com.cs.webservice.dto.auth;

import com.cs.webservice.dto.BaseResponse;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Setter
@NoArgsConstructor
public final class UserInformDTO {
    @AllArgsConstructor
    @NoArgsConstructor
    public static class CampaignNumber {
        @JsonProperty("accept")
        private int accept;

        @JsonProperty("reject")
        private int reject;

        @JsonProperty("participate")
        private int participate;
    }

    @JsonProperty("user_uuid")
    private String userUUID;

    @JsonProperty("user_id")
    private String userID;

    @JsonProperty("name")
    private String name;

    @JsonProperty("nick_name")
    private String nickName;

    @JsonProperty("email")
    private String email;

    @JsonProperty("profile_uri")
    private String profileURI;

    @JsonProperty("campaign_number")
    private CampaignNumber campaignNumber;
}
