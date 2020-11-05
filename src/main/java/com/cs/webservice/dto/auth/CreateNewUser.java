package com.cs.webservice.dto.auth;

import com.cs.webservice.dto.BaseResponse;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;
import org.springframework.web.multipart.MultipartFile;

import javax.validation.constraints.*;

public class CreateNewUser {
    @Getter
    @Setter
    @NoArgsConstructor
    public static class Request {
        @Size(min = 4, max = 20) @NotNull @NotEmpty
        @JsonProperty("user_id")
        private String userID;

        @Size(min = 4, max = 20) @NotNull @NotEmpty
        @JsonProperty("user_pw")
        private String userPW;

        @Size(min = 2, max = 5) @NotNull @NotEmpty
        @JsonProperty("name")
        private String name;

        @Size(max = 10) @NotNull @NotEmpty
        @JsonProperty("nick_name")
        private String nickName;

        @Size(max = 30) @NotNull @NotEmpty @Email
        @JsonProperty("email")
        private String email;

        @JsonProperty("profile")
        private MultipartFile profile;
    }

    @Setter
    @NoArgsConstructor
    public static class Response extends BaseResponse {
        @JsonProperty("user_uuid")
        private String userUUID;
    }
}
