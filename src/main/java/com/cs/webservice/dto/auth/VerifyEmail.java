package com.cs.webservice.dto.auth;

import com.cs.webservice.dto.BaseResponse;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;

public class VerifyEmail {
    @Getter
    @Setter
    @NoArgsConstructor
    public static class Request {
        @NotEmpty @NotNull @Email
        @JsonProperty("email")
        private String email;
        @NotEmpty @NotNull
        @JsonProperty("auth_code")
        private String authCode;
    }

    @NoArgsConstructor
    public static class Response extends BaseResponse { }
}
