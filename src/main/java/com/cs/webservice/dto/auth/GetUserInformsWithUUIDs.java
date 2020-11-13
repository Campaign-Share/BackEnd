package com.cs.webservice.dto.auth;

import com.cs.webservice.dto.BaseResponse;
import com.cs.webservice.dto.campaign.CampaignDTO;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.util.List;

public class GetUserInformsWithUUIDs {
    @Getter
    @Setter
    @NoArgsConstructor
    public static class Request {
        @NotNull
        @NotEmpty
        @JsonProperty("user_uuids")
        private List<String> userUUIDs;
    }

    @Setter
    @NoArgsConstructor
    public static class Response extends BaseResponse {
        @JsonProperty("user_informs")
        List<UserInformDTO> userInforms;
    }
}
