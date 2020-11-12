package com.cs.webservice.dto.campaign;

import com.cs.webservice.dto.BaseResponse;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.util.List;

public class GetCampaignWithUUID {
    @Setter
    @NoArgsConstructor
    public static class Response extends BaseResponse {
        @JsonProperty("campaign_uuid")
        private String campaignUUID;

        @JsonProperty("user_uuid")
        private String userUUID;

        @JsonProperty("state")
        private String state;

        @JsonProperty("title")
        private String title;

        @JsonProperty("sub_title")
        private String subTitle;

        @JsonProperty("introduction")
        private String introduction;

        @JsonProperty("participation")
        private String participation;

        @JsonProperty("start_date")
        private LocalDate startDate;

        @JsonProperty("end_date")
        private LocalDate endDate;

        @JsonProperty("post_uri")
        private String postURI;

        @JsonProperty("campaign_tags")
        private List<String> campaignTags;
    }
}
