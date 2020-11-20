package com.cs.webservice.dto.auth;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Setter
@Builder
public final class UserInformDTO {
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
    private CampaignNumberDTO campaignNumber;
}
