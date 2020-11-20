package com.cs.webservice.dto.auth;

import com.cs.webservice.dto.BaseResponse;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

public class ChangeUserPW {
    @Getter
    @Setter
    @NoArgsConstructor
    public static class Request {
        @NotNull @NotEmpty
        @JsonProperty("current_pw")
        private String currentPW;

        @Size(min = 4, max = 20) @NotNull @NotEmpty
        @JsonProperty("revision_pw")
        private String revisionPW;
    }

    @Setter
    @NoArgsConstructor
    public static class Response extends BaseResponse { }
}
