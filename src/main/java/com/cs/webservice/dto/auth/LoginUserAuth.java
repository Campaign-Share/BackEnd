package com.cs.webservice.dto.auth;

import com.cs.webservice.dto.BaseResponse;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;

public class LoginUserAuth {
    @Getter
    @Setter
    @NoArgsConstructor
    public static class Request {
        @NotNull @NotEmpty
        @JsonProperty("user_id")
        private String userID;

        @NotNull @NotEmpty
        @JsonProperty("user_pw")
        private String userPW;
    }

    @Setter
    @NoArgsConstructor
    public static class Response extends BaseResponse {
        @JsonProperty("user_uuid")
        private String userUUID;
        @JsonProperty("access_token")
        private String accessToken;
    }
}
