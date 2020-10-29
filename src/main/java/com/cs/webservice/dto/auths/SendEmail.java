package com.cs.webservice.dto.auths;

import com.cs.webservice.dto.BaseResponse;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

public class SendEmail {
    @Getter
    @Setter
    @NoArgsConstructor
    public static class Request {
        @Size(max = 30) @NotNull @NotEmpty @Email
        @JsonProperty("email")
        private String email;
    }

    @NoArgsConstructor
    public static class Response extends BaseResponse { }
}
