package com.cs.webservice.dto.auth;

import com.cs.webservice.dto.BaseResponse;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

public class ChangeUserInform {
    @Getter
    @Setter
    @NoArgsConstructor
    public static class Request {
        @Size(min = 2, max = 5)
        @JsonProperty("name")
        private String name;

        @Size(max = 10)
        @JsonProperty("nick_name")
        private String nickName;

        @Size(max = 30) @Email
        @JsonProperty("email")
        private String email;
    }

    @Setter
    @NoArgsConstructor
    public static class Response extends BaseResponse { }
}
