package com.cs.webservice.dto.campaign;

import com.cs.webservice.dto.BaseResponse;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.util.List;

public class GetCampaignsWithUUIDs {
    @Getter
    @Setter
    @NoArgsConstructor
    public static class Request {
        @NotNull
        @NotEmpty
        @JsonProperty("campaign_uuids")
        private List<String> campaignUUIDs;
    }

    @Setter
    @NoArgsConstructor
    public static class Response extends BaseResponse {
        @JsonProperty("campaigns")
        List<CampaignDTO> campaigns;
    }
}
