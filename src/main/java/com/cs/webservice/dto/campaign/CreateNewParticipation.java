package com.cs.webservice.dto.campaign;

import com.cs.webservice.dto.BaseResponse;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.web.multipart.MultipartFile;

import javax.validation.constraints.*;

public class CreateNewParticipation {
    @Getter
    @Setter
    @NoArgsConstructor
    public static class Request {
        @Size(max = 21) @NotNull @NotEmpty
        @JsonProperty("campaign_uuid")
        private String campaignUUID;

        @Size(max = 200) @NotNull @NotEmpty
        @JsonProperty("introduction")
        private String introduction;

        @JsonProperty("evidence")
        private MultipartFile evidence;
    }

    @Setter
    @NoArgsConstructor
    public static class Response extends BaseResponse {
        @JsonProperty("participation_uuid")
        private String participationUUID;
    }
}
