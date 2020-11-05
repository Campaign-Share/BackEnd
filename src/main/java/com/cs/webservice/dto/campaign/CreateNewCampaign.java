package com.cs.webservice.dto.campaign;

import com.cs.webservice.dto.BaseResponse;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.web.multipart.MultipartFile;

import javax.validation.constraints.*;

public class CreateNewCampaign {
    @Getter
    @Setter
    @NoArgsConstructor
    public static class Request {
        @Size(max = 50) @NotNull
        @JsonProperty("title")
        private String title;

        @Size(max = 50)
        @JsonProperty("sub_title")
        private String subTitle;

        @Size(max = 300) @NotNull @NotEmpty
        @JsonProperty("introduction")
        private String introduction;

        @Size(max = 300) @NotNull @NotEmpty
        @JsonProperty("participation")
        private String participation;

        @Min(0) @Max(30) @NotNull
        @JsonProperty("period_day")
        private Integer periodDay;

        @JsonProperty("poster")
        private MultipartFile poster;

        @JsonProperty("tags")
        private String tags; // tag1|tag2|tag3 (개달 20글자 이하)
    }

    @Setter
    @NoArgsConstructor
    public static class Response extends BaseResponse {
        @JsonProperty("campaign_uuid")
        private String campaignUUID;
    }
}
