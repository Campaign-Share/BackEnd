package com.cs.webservice.dto.campaign;

import com.cs.webservice.dto.BaseResponse;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

public class ReportCampaign {
    @Getter
    @Setter
    @NoArgsConstructor
    public static class Request {
        @Size(max = 20) @NotNull
        @NotEmpty
        @JsonProperty("field")
        private String field;

        @Size(max = 50) @NotNull @NotEmpty
        @JsonProperty("reason")
        private String reason;
    }

    @Setter
    @NoArgsConstructor
    public static class Response extends BaseResponse {
        @JsonProperty("report_uuid")
        private String reportUUID;
    }
}
