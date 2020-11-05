package com.cs.webservice.dto.campaign;

import com.cs.webservice.dto.BaseResponse;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.web.multipart.MultipartFile;

import javax.validation.constraints.*;
import java.time.LocalDate;
import java.util.List;

public class GetCampaignsWithUserUUID {
    @Setter
    @Builder
    public static class Campaign {
        @JsonProperty("campaign_uuid")
        private String campaignUUID;

        @JsonProperty("user_uuid")
        private String userUUID;

        @JsonProperty("accepted")
        private boolean accepted;

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

    @Setter
    @NoArgsConstructor
    public static class Response extends BaseResponse {
        @JsonProperty("campaigns")
        List<Campaign> campaigns;
    }
}
