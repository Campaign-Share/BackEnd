package com.cs.webservice.dto.campaign;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Setter;

import java.time.LocalDate;
import java.util.List;

@Setter
@Builder
public final class CampaignDTO {
    @JsonProperty("campaign_uuid")
    private String campaignUUID;

    @JsonProperty("user_uuid")
    private String userUUID;

    @JsonProperty("status")
    private String status;

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
