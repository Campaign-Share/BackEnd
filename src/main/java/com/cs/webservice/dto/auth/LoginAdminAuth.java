package com.cs.webservice.dto.auth;

import com.cs.webservice.dto.BaseResponse;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;

public class LoginAdminAuth {
    @Getter
    @Setter
    @NoArgsConstructor
    public static class Request {
        @NotNull
        @NotEmpty
        @JsonProperty("admin_id")
        private String userID;

        @NotNull @NotEmpty
        @JsonProperty("admin_pw")
        private String userPW;
    }

    @Setter
    @NoArgsConstructor
    public static class Response extends BaseResponse {
        @JsonProperty("admin_uuid")
        private String userUUID;
        @JsonProperty("access_token")
        private String accessToken;
    }
}
